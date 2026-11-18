package com.simats.focusflow.service

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent
import com.simats.focusflow.data.repository.ProductivityRepository
import com.simats.focusflow.ui.screens.FocusShieldActivity
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
    private var lastBlockedPkg: String? = null
    private var lastBlockActionTime = 0L

    private fun isIgnoredPackage(pkgName: String): Boolean {
        if (pkgName == packageName || pkgName == applicationContext.packageName) return true
        if (pkgName == "com.android.systemui") return true
        if (isLauncherPackage(pkgName)) return true
        return false
    }

    private fun isLauncherPackage(pkgName: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryIntentActivities(
                    intent,
                    android.content.pm.PackageManager.ResolveInfoFlags.of(android.content.pm.PackageManager.MATCH_DEFAULT_ONLY.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.queryIntentActivities(intent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
            }
            resolveInfos.any { it.activityInfo?.packageName == pkgName }
        } catch (_: Exception) {
            false
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val pkgName = event.packageName?.toString() ?: return
        if (isIgnoredPackage(pkgName)) return

        val timerState = FocusTimerService.timerStateFlow.value
        if (!timerState.isRunning || timerState.currentMode != TimerMode.FOCUS || !timerState.isStrictFocus) {
            return
        }

        if (UsageStatsHelper.isBlockedDistractingApp(applicationContext, pkgName)) {
            val now = System.currentTimeMillis()
            if (pkgName == lastBlockedPkg && (now - lastBlockActionTime < 1200L)) {
                return
            }
            lastBlockActionTime = now
            lastBlockedPkg = pkgName

            val appLabel = UsageStatsHelper.getAppLabel(applicationContext, pkgName)

            performGlobalAction(GLOBAL_ACTION_HOME)

            handler.postDelayed({
                try {
                    val shieldIntent = Intent(applicationContext, FocusShieldActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("EXTRA_BLOCKED_APP", appLabel)
                        putExtra("EXTRA_PACKAGE_NAME", pkgName)
                    }
                    startActivity(shieldIntent)
                } catch (_: Exception) {}
            }, 200L)

            if (now - lastLogTimestamp > 60000L) {
                lastLogTimestamp = now
                val activeUid = timerState.activeUserId
                val cat = UsageStatsHelper.categorizePackage(pkgName, appLabel, applicationContext)
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

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
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

        fun canDrawOverlays(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true
            }
        }

        fun openAccessibilitySettings(context: Context) {
            try {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }

        fun openOverlaySettings(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {
                    try {
                        val fallback = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(fallback)
                    } catch (_: Exception) {}
                }
            }
        }
    }
}
