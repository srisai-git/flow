package com.simats.focusflow.data.session

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.view.inputmethod.InputMethodManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BlockedAppsManager private constructor(private val context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        "focusflow_blocked_apps_prefs",
        Context.MODE_PRIVATE
    )

    private val _blockedPackagesFlow = MutableStateFlow(loadBlockedPackages())
    val blockedPackagesFlow: StateFlow<Set<String>> = _blockedPackagesFlow.asStateFlow()

    private fun loadBlockedPackages(): Set<String> {
        val isInitialized = prefs.getBoolean(KEY_INITIALIZED, false)
        if (!isInitialized) {
            val initial = mutableSetOf<String>()
            val candidates = listOf(
                "com.instagram.android",
                "com.google.android.youtube",
                "com.snapchat.android",
                "com.twitter.android",
                "com.facebook.katana",
                "com.zhiliaoapp.musically",
                "com.reddit.frontpage",
                "com.netflix.mediaclient",
                "tv.twitch.android.app",
                "com.discord",
                "org.telegram.messenger"
            )
            for (pkg in candidates) {
                if (isPackageInstalled(context, pkg) && !isSystemOrEssentialPackage(context, pkg)) {
                    initial.add(pkg)
                }
            }
            prefs.edit()
                .putStringSet(KEY_BLOCKED_PACKAGES, initial)
                .putBoolean(KEY_INITIALIZED, true)
                .apply()
            return initial
        }
        return prefs.getStringSet(KEY_BLOCKED_PACKAGES, emptySet())?.toSet() ?: emptySet()
    }

    fun getBlockedPackages(): Set<String> {
        return _blockedPackagesFlow.value
    }

    fun isAppBlocked(packageName: String): Boolean {
        if (isSystemOrEssentialPackage(context, packageName)) return false
        return _blockedPackagesFlow.value.contains(packageName)
    }

    fun setAppBlocked(packageName: String, blocked: Boolean) {
        if (isSystemOrEssentialPackage(context, packageName)) return
        val current = _blockedPackagesFlow.value.toMutableSet()
        if (blocked) {
            current.add(packageName)
        } else {
            current.remove(packageName)
        }
        prefs.edit().putStringSet(KEY_BLOCKED_PACKAGES, current).apply()
        _blockedPackagesFlow.value = current
    }

    fun setBlockedPackages(packages: Set<String>) {
        val filtered = packages.filterNot { isSystemOrEssentialPackage(context, it) }.toSet()
        prefs.edit().putStringSet(KEY_BLOCKED_PACKAGES, filtered).apply()
        _blockedPackagesFlow.value = filtered
    }

    companion object {
        private const val KEY_INITIALIZED = "key_blocked_apps_initialized"
        private const val KEY_BLOCKED_PACKAGES = "key_blocked_packages_set"

        private val essentialPackages = setOf(
            "android",
            "com.android.systemui",
            "com.google.android.systemui",
            "com.android.settings",
            "com.google.android.settings",
            "com.android.packageinstaller",
            "com.google.android.packageinstaller",
            "com.android.permissioncontroller",
            "com.google.android.permissioncontroller",
            "com.android.dialer",
            "com.google.android.dialer",
            "com.samsung.android.dialer",
            "com.android.incallui",
            "com.google.android.incallui",
            "com.android.contacts",
            "com.google.android.contacts",
            "com.android.deskclock",
            "com.google.android.deskclock",
            "com.sec.android.app.clockpackage",
            "com.android.camera",
            "com.google.android.GoogleCamera",
            "com.sec.android.app.camera"
        )

        fun isPackageInstalled(context: Context, packageName: String): Boolean {
            return try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0L))
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.getPackageInfo(packageName, 0)
                }
                true
            } catch (_: Exception) {
                false
            }
        }

        fun isLauncherPackage(context: Context, packageName: String): Boolean {
            return try {
                val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.packageManager.queryIntentActivities(
                        intent,
                        PackageManager.ResolveInfoFlags.of(0L)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.queryIntentActivities(intent, 0)
                }
                resolveInfos.any { it.activityInfo?.packageName == packageName }
            } catch (_: Exception) {
                false
            }
        }

        fun isImePackage(context: Context, packageName: String): Boolean {
            return try {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.enabledInputMethodList?.any { it.packageName == packageName } == true
            } catch (_: Exception) {
                false
            }
        }

        fun isSystemOrEssentialPackage(context: Context, packageName: String): Boolean {
            if (packageName == context.packageName || packageName == context.applicationContext.packageName) return true
            if (essentialPackages.contains(packageName)) return true
            if (isLauncherPackage(context, packageName)) return true
            if (isImePackage(context, packageName)) return true
            return false
        }

        @Volatile
        private var INSTANCE: BlockedAppsManager? = null

        fun getInstance(context: Context): BlockedAppsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BlockedAppsManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
