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

        val blockedAppsManager = BlockedAppsManager.getInstance(applicationContext)
        val eventPkg = event.packageName?.toString()
        val rootPkg = try { rootInActiveWindow?.packageName?.toString() } catch (_: Exception) { null }

        val targetPkg = when {
            rootPkg != null && isBlockedPackage(rootPkg, blockedAppsManager) -> rootPkg
            eventPkg != null && isBlockedPackage(eventPkg, blockedAppsManager) -> eventPkg
            else -> null
        }

        if (targetPkg != null) {
            performGlobalAction(GLOBAL_ACTION_HOME)

            handler.postDelayed({
                val activePkg = try { rootInActiveWindow?.packageName?.toString() } catch (_: Exception) { null }
                if (activePkg != null && isBlockedPackage(activePkg, blockedAppsManager)) {
                    performGlobalAction(GLOBAL_ACTION_HOME)
                }
            }, 150L)

            handler.postDelayed({
                val activePkg = try { rootInActiveWindow?.packageName?.toString() } catch (_: Exception) { null }
                if (activePkg != null && isBlockedPackage(activePkg, blockedAppsManager)) {
                    performGlobalAction(GLOBAL_ACTION_HOME)
                }
            }, 350L)

            val now = System.currentTimeMillis()
            if (now - lastLogTimestamp > 15000L) {
                lastLogTimestamp = now
                val activeUid = timerState.activeUserId
                val appLabel = if (targetPkg == "com.android.settings") "Settings"
                    else if (targetPkg == "com.android.vending") "Google Play Store"
                    else UsageStatsHelper.getAppLabel(applicationContext, targetPkg)
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

    private fun isBlockedPackage(pkg: String, blockedAppsManager: BlockedAppsManager): Boolean {
        if (pkg == applicationContext.packageName) return false
        if (pkg == "com.android.systemui") return false
        if (pkg == "com.android.settings" || pkg == "com.android.vending") return true
        if (BlockedAppsManager.isSystemOrEssentialPackage(applicationContext, pkg)) return false
        return blockedAppsManager.isAppBlocked(pkg)
    }

    private fun isAllowedPackage(pkg: String): Boolean {
        if (pkg == applicationContext.packageName) return true
        if (pkg == "com.android.systemui") return true
        if (pkg == "com.android.settings" || pkg == "com.android.vending") return false
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
