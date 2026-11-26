package com.simats.focusflow.service

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.simats.focusflow.data.repository.ProductivityRepository
import com.simats.focusflow.data.session.BlockedAppsManager
import com.simats.focusflow.ui.viewmodel.TimerMode
import com.simats.focusflow.utils.UsageStatsHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FocusBlockerAccessibilityService : AccessibilityService() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private val repository = ProductivityRepository()
    private val handler = Handler(Looper.getMainLooper())
    private var lastLogTimestamp = 0L

    private val blockedInstallerAndSettingsPackages = setOf(
        "com.android.settings",
        "com.android.vending",
        "com.android.packageinstaller",
        "com.google.android.packageinstaller",
        "com.oplus.appdetail",
        "com.oplus.settings",
        "com.coloros.safecenter",
        "com.miui.packageinstaller",
        "com.miui.securitycenter",
        "com.samsung.android.packageinstaller"
    )

    private fun isInstallerOrSettingsPackage(pkg: String): Boolean {
        if (blockedInstallerAndSettingsPackages.contains(pkg)) return true
        if (pkg.contains("packageinstaller") || pkg.contains("appdetail")) return true
        return false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val eventType = event.eventType
        if (eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED) {
            return
        }

        val timerState = FocusTimerService.timerStateFlow.value
        if (!timerState.isRunning || timerState.currentMode != TimerMode.FOCUS || !timerState.isStrictFocus) {
            return
        }

        if (timerState.isUninstallLock && !FlowDeviceAdminReceiver.isDeviceAdminActive(applicationContext)) {
            FlowDeviceAdminReceiver.requestDeviceAdmin(applicationContext)
        }

        if (timerState.isUninstallLock && isUninstallDialogEvent(event)) {
            performGlobalAction(GLOBAL_ACTION_BACK)
            performGlobalAction(GLOBAL_ACTION_HOME)
            handler.postDelayed({
                performGlobalAction(GLOBAL_ACTION_BACK)
                performGlobalAction(GLOBAL_ACTION_HOME)
            }, 100L)
            return
        }

        val blockedAppsManager = BlockedAppsManager.getInstance(applicationContext)
        val eventPkg = event.packageName?.toString()
        val rootPkg = try { rootInActiveWindow?.packageName?.toString() } catch (_: Exception) { null }

        val targetPkg = when {
            rootPkg != null && isBlockedPackage(rootPkg, blockedAppsManager, timerState.isUninstallLock) -> rootPkg
            eventPkg != null && isBlockedPackage(eventPkg, blockedAppsManager, timerState.isUninstallLock) -> eventPkg
            else -> null
        }

        if (targetPkg != null) {
            performGlobalAction(GLOBAL_ACTION_HOME)

            handler.postDelayed({
                val activePkg = try { rootInActiveWindow?.packageName?.toString() } catch (_: Exception) { null }
                if (activePkg != null && isBlockedPackage(activePkg, blockedAppsManager, timerState.isUninstallLock)) {
                    performGlobalAction(GLOBAL_ACTION_HOME)
                }
            }, 150L)

            handler.postDelayed({
                val activePkg = try { rootInActiveWindow?.packageName?.toString() } catch (_: Exception) { null }
                if (activePkg != null && isBlockedPackage(activePkg, blockedAppsManager, timerState.isUninstallLock)) {
                    performGlobalAction(GLOBAL_ACTION_HOME)
                }
            }, 350L)

            val now = System.currentTimeMillis()
            if (now - lastLogTimestamp > 15000L) {
                lastLogTimestamp = now
                val activeUid = timerState.activeUserId
                val appLabel = when {
                    isInstallerOrSettingsPackage(targetPkg) -> "Settings & Installer Guard"
                    else -> UsageStatsHelper.getAppLabel(applicationContext, targetPkg)
                }
                val cat = UsageStatsHelper.categorizePackage(targetPkg, appLabel, applicationContext)
                serviceScope.launch(Dispatchers.IO) {
                    repository.logDistraction(
                        userId = activeUid,
                        reason = "Blocked app opened: $appLabel",
                        category = cat,
                        durationSeconds = 0
                    )
                }
            }
        }
    }

    private fun isUninstallDialogEvent(event: AccessibilityEvent): Boolean {
        val sb = StringBuilder()
        event.text?.forEach { sb.append(it).append(" ") }
        event.contentDescription?.let { sb.append(it).append(" ") }

        val root = try { rootInActiveWindow } catch (_: Exception) { null }
        if (root != null) {
            sb.append(extractNodeText(root))
            try { root.recycle() } catch (_: Exception) {}
        }

        val source = try { event.source } catch (_: Exception) { null }
        if (source != null) {
            sb.append(extractNodeText(source))
            try { source.recycle() } catch (_: Exception) {}
        }

        val text = sb.toString().lowercase()
        val hasFlow = text.contains("flow")
        val hasUninstallOrRemove = text.contains("uninstall") || text.contains("remove")
        return hasFlow && hasUninstallOrRemove
    }

    private fun extractNodeText(node: AccessibilityNodeInfo?, depth: Int = 0): String {
        if (node == null || depth > 8) return ""
        val sb = StringBuilder()
        node.text?.let { sb.append(it).append(" ") }
        node.contentDescription?.let { sb.append(it).append(" ") }
        for (i in 0 until node.childCount) {
            val child = try { node.getChild(i) } catch (_: Exception) { null }
            if (child != null) {
                sb.append(extractNodeText(child, depth + 1))
                try { child.recycle() } catch (_: Exception) {}
            }
        }
        return sb.toString()
    }

    private fun isBlockedPackage(pkg: String, blockedAppsManager: BlockedAppsManager, isUninstallLock: Boolean): Boolean {
        if (pkg == applicationContext.packageName) return false
        if (pkg == "com.android.systemui") return false
        if (isUninstallLock && isInstallerOrSettingsPackage(pkg)) return true
        if (BlockedAppsManager.isSystemOrEssentialPackage(applicationContext, pkg)) return false
        return blockedAppsManager.isAppBlocked(pkg)
    }

    private fun isAllowedPackage(pkg: String, isUninstallLock: Boolean): Boolean {
        if (pkg == applicationContext.packageName) return true
        if (pkg == "com.android.systemui") return true
        if (isUninstallLock && isInstallerOrSettingsPackage(pkg)) return false
        if (BlockedAppsManager.isSystemOrEssentialPackage(applicationContext, pkg)) return true
        return false
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        serviceJob.cancel()
    }

    companion object {
        fun isAccessibilityServiceEnabled(context: Context): Boolean {
            val expectedComponentName = ComponentName(context, FocusBlockerAccessibilityService::class.java)
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServices)
            while (colonSplitter.hasNext()) {
                val componentNameString = colonSplitter.next()
                val enabledComponent = ComponentName.unflattenFromString(componentNameString)
                if (enabledComponent != null && enabledComponent == expectedComponentName) {
                    return true
                }
            }
            return false
        }

        fun openAccessibilitySettings(context: Context) {
            try {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }
}
