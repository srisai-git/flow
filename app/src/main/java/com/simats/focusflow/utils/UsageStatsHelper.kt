package com.simats.focusflow.utils

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.collection.LruCache
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val usageMinutes: Int,
    val category: String,
    val percent: Int = 0,
    val icon: Drawable? = null
)

data class RingSegment(
    val label: String,
    val packageName: String?,
    val minutes: Int,
    val percentage: Float
)

data class AppEventTransition(
    val packageName: String,
    val eventType: Int,
    val timestamp: Long
)

data class DistractionUsageSummary(
    val totalScreenMinutes: Int = 0,
    val totalDistractionMinutes: Int = 0,
    val topCategory: String = "Social Media",
    val apps: List<AppUsageInfo> = emptyList(),
    val categoryBreakdown: Map<String, Int> = emptyMap()
) {
    

    fun computeRingSegments(maxTopApps: Int = 4): List<RingSegment> {
        val active = apps.filter { it.usageMinutes > 0 }
        if (totalScreenMinutes <= 0 || active.isEmpty()) return emptyList()

        val topApps = active.take(maxTopApps)
        val otherApps = active.drop(maxTopApps)
        val segments = mutableListOf<RingSegment>()

        for (app in topApps) {
            val pct = (app.usageMinutes.toFloat() / totalScreenMinutes.toFloat())
            segments.add(
                RingSegment(
                    label = app.appName,
                    packageName = app.packageName,
                    minutes = app.usageMinutes,
                    percentage = pct
                )
            )
        }

        val otherMinutes = otherApps.sumOf { it.usageMinutes }
        if (otherMinutes > 0) {
            val pct = (otherMinutes.toFloat() / totalScreenMinutes.toFloat())
            segments.add(
                RingSegment(
                    label = "Other",
                    packageName = null,
                    minutes = otherMinutes,
                    percentage = pct
                )
            )
        }

        return segments
    }
}

object UsageStatsHelper {

    private val iconCache = LruCache<String, Drawable>(150)
    private val labelCache = ConcurrentHashMap<String, String>()

    fun getCachedIcon(packageName: String): Drawable? = iconCache.get(packageName)
    fun putCachedIcon(packageName: String, icon: Drawable) { iconCache.put(packageName, icon) }
    fun getCachedLabel(packageName: String): String? = labelCache[packageName]
    fun putCachedLabel(packageName: String, label: String) { labelCache[packageName] = label }
    fun clearCache() {
        iconCache.evictAll()
        labelCache.clear()
    }

    

    fun getExactMidnightTimestamp(): Long {
        return LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun getStartOfDayBoundary(): Long = getExactMidnightTimestamp()
    fun getStrictNowTimestamp(): Long = System.currentTimeMillis()

    

    fun getTodayStartTimestamp(): Long = getExactMidnightTimestamp()

    

    fun computeSafeForegroundDelta(
        resumeTime: Long,
        pauseTime: Long,
        windowStartTime: Long,
        windowEndTime: Long
    ): Long {
        val effectiveStart = maxOf(windowStartTime, resumeTime)
        val effectiveEnd = minOf(windowEndTime, pauseTime)
        return if (effectiveEnd > effectiveStart) effectiveEnd - effectiveStart else 0L
    }

    

    fun getLiveBoundedEndTime(): Long = System.currentTimeMillis()

    

    const val EVENT_ACTIVITY_RESUMED = 1
    const val EVENT_MOVE_TO_FOREGROUND = 1
    const val EVENT_ACTIVITY_PAUSED = 2
    const val EVENT_ACTIVITY_STOPPED = 23
    const val EVENT_MOVE_TO_BACKGROUND = 2
    const val EVENT_SCREEN_NON_INTERACTIVE = 16
    const val EVENT_KEYGUARD_SHOWN = 17

    val SYSTEM_IGNORED_PACKAGES = setOf(
        "android",
        "com.android.systemui",
        "com.google.android.inputmethod.latin",
        "com.samsung.android.honeyboard",
        "com.google.android.apps.nexuslauncher",
        "com.sec.android.app.launcher",
        "com.android.launcher3",
        "com.google.android.googlequicksearchbox",
        "com.google.android.permissioncontroller"
    )

    fun isSystemProcessOrDaemon(pkg: String): Boolean {
        if (SYSTEM_IGNORED_PACKAGES.contains(pkg)) return true
        val lower = pkg.lowercase()
        return lower.contains("systemui") || lower.contains(".daemon.") || lower.contains("telephony") ||
                lower.contains("wallpaper") || lower.contains("inputmethod") || lower.contains("honeyboard")
    }

    fun getDefaultHomeLauncherPackage(context: Context): String? {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val resolveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.resolveActivity(
                homeIntent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
        }
        return resolveInfo?.activityInfo?.packageName
    }

    fun getAllHomeLaunchers(context: Context): Set<String> {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val resolveInfos = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.queryIntentActivities(homeIntent, PackageManager.ResolveInfoFlags.of(0L))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.queryIntentActivities(homeIntent, 0)
            }
        } catch (_: Exception) {
            emptyList()
        }
        val set = resolveInfos.mapNotNull { it.activityInfo?.packageName }.toMutableSet()
        getDefaultHomeLauncherPackage(context)?.let { set.add(it) }
        return set
    }

    fun isExcludedApp(
        context: Context,
        pkg: String,
        defaultHome: String?,
        homeLaunchers: Set<String>
    ): Boolean {
        if (pkg.isBlank()) return true
        if (pkg == context.packageName) return true
        if (pkg == "com.android.systemui") return true
        if (pkg == "com.android.settings") return true
        if (pkg == "android") return true
        if (pkg == defaultHome || homeLaunchers.contains(pkg)) return true
        return isSystemProcessOrDaemon(pkg)
    }

    fun getAppIcon(context: Context, packageName: String): Drawable? {
        getCachedIcon(packageName)?.let { return it }
        return try {
            val ic = context.packageManager.getApplicationIcon(packageName)
            putCachedIcon(packageName, ic)
            ic
        } catch (_: Exception) {
            null
        }
    }

    fun queryForegroundTimesFromEvents(
        context: Context,
        startTime: Long,
        endTime: Long
    ): Map<String, Long> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyMap()

        val liveNow = minOf(endTime, System.currentTimeMillis())
        if (liveNow <= startTime) return emptyMap()

        val rawEvents = try {
            usageStatsManager.queryEvents(startTime, liveNow)
        } catch (_: Exception) {
            null
        } ?: return emptyMap()

        val eventList = mutableListOf<AppEventTransition>()
        val tempEvt = android.app.usage.UsageEvents.Event()
        while (rawEvents.hasNextEvent()) {
            rawEvents.getNextEvent(tempEvt)
            val p = tempEvt.packageName
            eventList.add(AppEventTransition(p ?: "", tempEvt.eventType, tempEvt.timeStamp))
        }
        eventList.sortBy { it.timestamp }

        var currentPackage: String? = null
        var sessionStartMillis = 0L
        val usageMap = mutableMapOf<String, Long>()

        for (event in eventList) {
            val pkg = event.packageName
            val eventType = event.eventType
            val time = event.timestamp

            val isForeground = eventType == android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED ||
                    eventType == EVENT_MOVE_TO_FOREGROUND
            val isBackground = eventType == android.app.usage.UsageEvents.Event.ACTIVITY_PAUSED ||
                    eventType == EVENT_ACTIVITY_STOPPED
            val isScreenOff = eventType == EVENT_SCREEN_NON_INTERACTIVE ||
                    eventType == EVENT_KEYGUARD_SHOWN

            if (isForeground && pkg.isNotBlank()) {
                if (currentPackage != null && currentPackage != pkg) {
                    if (sessionStartMillis >= startTime && sessionStartMillis > 0L) {
                        val boundedEnd = minOf(time, liveNow)
                        val delta = (boundedEnd - sessionStartMillis).coerceAtLeast(0L)
                        if (delta > 0L) {
                            usageMap[currentPackage!!] = (usageMap[currentPackage!!] ?: 0L) + delta
                        }
                    }
                    currentPackage = pkg
                    sessionStartMillis = maxOf(time, startTime)
                } else if (currentPackage == null) {
                    currentPackage = pkg
                    sessionStartMillis = maxOf(time, startTime)
                }
            } else if (isBackground && pkg.isNotBlank()) {
                if (currentPackage == pkg) {
                    if (sessionStartMillis >= startTime && sessionStartMillis > 0L) {
                        val boundedEnd = minOf(time, liveNow)
                        val delta = (boundedEnd - sessionStartMillis).coerceAtLeast(0L)
                        if (delta > 0L) {
                            usageMap[pkg] = (usageMap[pkg] ?: 0L) + delta
                        }
                    }
                    currentPackage = null
                    sessionStartMillis = 0L
                }
            } else if (isScreenOff) {
                if (currentPackage != null) {
                    if (sessionStartMillis >= startTime && sessionStartMillis > 0L) {
                        val boundedEnd = minOf(time, liveNow)
                        val delta = (boundedEnd - sessionStartMillis).coerceAtLeast(0L)
                        if (delta > 0L) {
                            usageMap[currentPackage!!] = (usageMap[currentPackage!!] ?: 0L) + delta
                        }
                    }
                    currentPackage = null
                    sessionStartMillis = 0L
                }
            }
        }

        if (currentPackage != null && sessionStartMillis >= startTime && sessionStartMillis > 0L) {
            val delta = (liveNow - sessionStartMillis).coerceAtLeast(0L)
            if (delta > 0L) {
                usageMap[currentPackage!!] = (usageMap[currentPackage!!] ?: 0L) + delta
            }
        }

        return usageMap
    }

    fun hasUsageStatsPermission(context: Context): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (_: Exception) {
            false
        }
    }

    fun openUsageAccessSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallback)
        }
    }

    

    fun getLauncherPackageNames(context: Context): Set<String> {
        val pm = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos: List<ResolveInfo> = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(launcherIntent, PackageManager.ResolveInfoFlags.of(0L))
            } else {
                @Suppress("DEPRECATION")
                pm.queryIntentActivities(launcherIntent, 0)
            }
        } catch (_: Exception) {
            emptyList()
        }
        return resolveInfos.mapNotNull { it.activityInfo?.packageName }.toSet()
    }
    

    fun isUserFacingApp(context: Context, packageName: String): Boolean {
        if (packageName == context.packageName) return false
        val launcherPackages = getLauncherPackageNames(context)
        if (launcherPackages.contains(packageName)) return true
        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) return true
        val appInfo = try {
            pm.getApplicationInfo(packageName, 0)
        } catch (_: Exception) {
            null
        } ?: return false
        val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        return !isSystem
    }

    

    fun queryAllInstalledLauncherApps(context: Context): List<AppUsageInfo> {
        val pm = context.packageManager
        val selfPkg = context.packageName

        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos: List<ResolveInfo> = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(launcherIntent, PackageManager.ResolveInfoFlags.of(0L))
            } else {
                @Suppress("DEPRECATION")
                pm.queryIntentActivities(launcherIntent, 0)
            }
        } catch (_: Exception) {
            emptyList()
        }

        val seenPackages = mutableSetOf<String>()
        val appList = mutableListOf<AppUsageInfo>()

        for (resolveInfo in resolveInfos) {
            val pkg = resolveInfo.activityInfo?.packageName ?: continue
            if (pkg == selfPkg || seenPackages.contains(pkg)) continue
            seenPackages.add(pkg)

            val appName = try {
                resolveInfo.loadLabel(pm).toString()
            } catch (_: Exception) {
                pkg.substringAfterLast('.')
            }

            val appIcon: Drawable? = try {
                resolveInfo.loadIcon(pm)
            } catch (_: Exception) {
                null
            }

            val category = categorizePackage(pkg, appName)
            appList.add(
                AppUsageInfo(
                    packageName = pkg,
                    appName = appName,
                    usageMinutes = 0,
                    category = category,
                    icon = appIcon
                )
            )
        }

        return appList.sortedBy { it.appName.lowercase() }
    }

    

    fun queryTodayDistractionStats(context: Context): DistractionUsageSummary {
        val pm = context.packageManager

        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos: List<ResolveInfo> = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(launcherIntent, PackageManager.ResolveInfoFlags.of(0L))
            } else {
                @Suppress("DEPRECATION")
                pm.queryIntentActivities(launcherIntent, 0)
            }
        } catch (_: Exception) {
            emptyList()
        }

        val launcherAppMap = mutableMapOf<String, Pair<String, Drawable?>>()
        for (resolveInfo in resolveInfos) {
            val pkg = resolveInfo.activityInfo?.packageName ?: continue
            if (launcherAppMap.containsKey(pkg)) continue

            val label = getCachedLabel(pkg) ?: run {
                val l = try {
                    resolveInfo.loadLabel(pm).toString()
                } catch (_: Exception) {
                    pkg.substringAfterLast('.')
                }
                putCachedLabel(pkg, l)
                l
            }

            val icon = getCachedIcon(pkg) ?: run {
                val ic = try {
                    resolveInfo.loadIcon(pm)
                } catch (_: Exception) {
                    null
                }
                if (ic != null) putCachedIcon(pkg, ic)
                ic
            }

            launcherAppMap[pkg] = Pair(label, icon)
        }

        if (!hasUsageStatsPermission(context)) {
            val unmeasuredApps = launcherAppMap.entries.map { (pkg, pair) ->
                AppUsageInfo(
                    packageName = pkg,
                    appName = pair.first,
                    usageMinutes = 0,
                    category = categorizePackage(pkg, pair.first),
                    icon = pair.second
                )
            }.sortedBy { it.appName.lowercase() }

            return DistractionUsageSummary(
                apps = unmeasuredApps
            )
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return DistractionUsageSummary()

        val startOfTodayMillis = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = System.currentTimeMillis()

        val dailyStatsList = try {
            usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startOfTodayMillis, endMillis)
        } catch (_: Exception) {
            null
        }
        val dailyMap = dailyStatsList?.associate { it.packageName to it.totalTimeInForeground } ?: emptyMap()

        val events = try {
            usageStatsManager.queryEvents(startOfTodayMillis, endMillis)
        } catch (_: Exception) {
            null
        } ?: return DistractionUsageSummary()

        val eventList = mutableListOf<AppEventTransition>()
        val tempEvt = android.app.usage.UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(tempEvt)
            val p = tempEvt.packageName
            eventList.add(AppEventTransition(p ?: "", tempEvt.eventType, tempEvt.timeStamp))
        }
        eventList.sortBy { it.timestamp }

        var currentPackage: String? = null
        var sessionStartMillis = 0L
        var lastScreenOffMillis = 0L
        val usageMap = mutableMapOf<String, Long>()

        for (event in eventList) {
            val pkg = event.packageName
            val eventType = event.eventType
            val time = event.timestamp

            val isForeground = eventType == android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED ||
                    eventType == EVENT_MOVE_TO_FOREGROUND
            val isBackground = eventType == android.app.usage.UsageEvents.Event.ACTIVITY_PAUSED ||
                    eventType == EVENT_ACTIVITY_STOPPED
            val isScreenOff = eventType == EVENT_SCREEN_NON_INTERACTIVE ||
                    eventType == EVENT_KEYGUARD_SHOWN

            if (isForeground && pkg.isNotBlank()) {
                if (currentPackage != null && currentPackage != pkg) {
                    if (sessionStartMillis >= startOfTodayMillis && sessionStartMillis > 0L) {
                        val boundedEnd = minOf(time, endMillis)
                        val delta = (boundedEnd - sessionStartMillis).coerceAtLeast(0L)
                        if (delta >= 60_000L) {
                            usageMap[currentPackage!!] = (usageMap[currentPackage!!] ?: 0L) + delta
                        }
                    }
                    currentPackage = pkg
                    sessionStartMillis = maxOf(time, startOfTodayMillis)
                } else if (currentPackage == null) {
                    currentPackage = pkg
                    sessionStartMillis = maxOf(time, startOfTodayMillis)
                }
            } else if (isBackground && pkg.isNotBlank()) {
                if (currentPackage == pkg) {
                    if (sessionStartMillis >= startOfTodayMillis && sessionStartMillis > 0L) {
                        val boundedEnd = minOf(time, endMillis)
                        val delta = (boundedEnd - sessionStartMillis).coerceAtLeast(0L)
                        if (delta >= 60_000L) {
                            usageMap[pkg] = (usageMap[pkg] ?: 0L) + delta
                        }
                    }
                    currentPackage = null
                    sessionStartMillis = 0L
                }
            } else if (isScreenOff) {
                if (currentPackage != null) {
                    if (sessionStartMillis >= startOfTodayMillis && sessionStartMillis > 0L) {
                        val boundedEnd = minOf(time, endMillis)
                        val delta = (boundedEnd - sessionStartMillis).coerceAtLeast(0L)
                        if (delta >= 60_000L) {
                            usageMap[currentPackage!!] = (usageMap[currentPackage!!] ?: 0L) + delta
                        }
                    }
                    currentPackage = null
                    sessionStartMillis = 0L
                }
                lastScreenOffMillis = time
            }
        }

        if (currentPackage != null && sessionStartMillis >= startOfTodayMillis && sessionStartMillis > 0L) {
            if (sessionStartMillis >= lastScreenOffMillis) {
                val delta = (endMillis - sessionStartMillis).coerceAtLeast(0L)
                if (delta >= 60_000L) {
                    usageMap[currentPackage!!] = (usageMap[currentPackage!!] ?: 0L) + delta
                }
            }
        }

        val combinedKeys = (usageMap.keys + dailyMap.keys).toSet()
        val finalUsageMap = mutableMapOf<String, Long>()
        for (pkg in combinedKeys) {
            val eventDuration = usageMap[pkg] ?: 0L
            val dailyDuration = dailyMap[pkg] ?: 0L
            val effectiveDuration = if (eventDuration > 0L && dailyDuration > 0L) {
                minOf(eventDuration, dailyDuration + 120_000L)
            } else if (eventDuration > 0L) {
                eventDuration
            } else if (dailyDuration >= 60_000L) {
                dailyDuration
            } else {
                0L
            }
            if (effectiveDuration >= 60_000L) {
                finalUsageMap[pkg] = effectiveDuration
            }
        }

        val defaultHome = getDefaultHomeLauncherPackage(context)
        val homeLaunchers = getAllHomeLaunchers(context)

        val appInfos = mutableListOf<AppUsageInfo>()

        for ((pkg, durationMs) in finalUsageMap) {
            if (isExcludedApp(context, pkg, defaultHome, homeLaunchers)) continue
            if (durationMs < 60_000L) continue

            val usageMinutes = (durationMs / 60_000L).toInt()
            if (usageMinutes <= 0) continue

            val appName = launcherAppMap[pkg]?.first ?: getAppLabel(context, pkg)
            val appIcon = launcherAppMap[pkg]?.second ?: getAppIcon(context, pkg)
            val category = categorizePackage(pkg, appName, context)

            appInfos.add(
                AppUsageInfo(
                    packageName = pkg,
                    appName = appName,
                    usageMinutes = usageMinutes,
                    category = category,
                    icon = appIcon
                )
            )
        }

        val sortedApps = appInfos.sortedByDescending { it.usageMinutes }
        val totalScreenMinutes = sortedApps.sumOf { it.usageMinutes }

        val appsWithPercent = sortedApps.map {
            val pct = if (totalScreenMinutes > 0 && it.usageMinutes > 0) {
                ((it.usageMinutes.toFloat() / totalScreenMinutes.toFloat()) * 100).toInt().coerceIn(0, 100)
            } else 0
            it.copy(percent = pct)
        }

        val categoryBreakdown = mutableMapOf<String, Int>()
        for (app in appsWithPercent) {
            categoryBreakdown[app.category] = (categoryBreakdown[app.category] ?: 0) + app.usageMinutes
        }

        val topCategory = categoryBreakdown.maxByOrNull { it.value }?.key ?: "Social Media"

        return DistractionUsageSummary(
            totalScreenMinutes = totalScreenMinutes,
            totalDistractionMinutes = totalScreenMinutes,
            topCategory = topCategory,
            apps = appsWithPercent,
            categoryBreakdown = categoryBreakdown
        )
    }

    fun generateGrootDistractionInsight(summary: DistractionUsageSummary): String {
        val topApp = summary.apps.firstOrNull { it.usageMinutes > 0 }
        return when {
            topApp != null && topApp.usageMinutes >= 60 -> {
                val hours = topApp.usageMinutes / 60
                val mins = topApp.usageMinutes % 60
                val durationStr = if (mins > 0) "${hours}h ${mins}m" else "${hours}h"
                "Groot Insight: ${topApp.appName} took up ${topApp.percent}% of your screen time today ($durationStr). Consider setting a daily app limit before your next focus block."
            }
            topApp != null && topApp.usageMinutes > 0 -> {
                "Groot Insight: ${topApp.appName} took up ${topApp.percent}% of your screen time today (${topApp.usageMinutes}m). Keep your focus momentum going by scheduling distraction-free intervals."
            }
            summary.totalScreenMinutes > 0 -> {
                "Groot Insight: You have logged ${summary.totalScreenMinutes}m of screen time across apps today. Take mindful breaks to protect your energy."
            }
            else -> {
                "Groot Insight: Zero app distractions logged today. Exceptional focus discipline! Keep up the great work."
            }
        }
    }

    fun queryWeeklyDistractionStats(context: Context): DistractionUsageSummary {
        val pm = context.packageManager
        val selfPkg = context.packageName

        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos: List<ResolveInfo> = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(launcherIntent, PackageManager.ResolveInfoFlags.of(0L))
            } else {
                @Suppress("DEPRECATION")
                pm.queryIntentActivities(launcherIntent, 0)
            }
        } catch (_: Exception) {
            emptyList()
        }

        val launcherAppMap = mutableMapOf<String, Pair<String, Drawable?>>()
        for (resolveInfo in resolveInfos) {
            val pkg = resolveInfo.activityInfo?.packageName ?: continue
            if (pkg == selfPkg || launcherAppMap.containsKey(pkg)) continue

            val label = getCachedLabel(pkg) ?: run {
                val l = try {
                    resolveInfo.loadLabel(pm).toString()
                } catch (_: Exception) {
                    pkg.substringAfterLast('.')
                }
                putCachedLabel(pkg, l)
                l
            }

            val icon = getCachedIcon(pkg) ?: run {
                val ic = try {
                    resolveInfo.loadIcon(pm)
                } catch (_: Exception) {
                    null
                }
                if (ic != null) putCachedIcon(pkg, ic)
                ic
            }

            launcherAppMap[pkg] = Pair(label, icon)
        }

        if (!hasUsageStatsPermission(context)) {
            val unmeasuredApps = launcherAppMap.entries.map { (pkg, pair) ->
                AppUsageInfo(
                    packageName = pkg,
                    appName = pair.first,
                    usageMinutes = 0,
                    category = categorizePackage(pkg, pair.first),
                    icon = pair.second
                )
            }.sortedBy { it.appName.lowercase() }

            return DistractionUsageSummary(
                apps = unmeasuredApps
            )
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return DistractionUsageSummary()

        val cal = Calendar.getInstance()
        val endTime = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, -7)
        val startTime = cal.timeInMillis

        var usageStatsList: List<UsageStats> = try {
            usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            ) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }

        if (usageStatsList.isEmpty()) {
            val aggregated = try {
                usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)
            } catch (_: Exception) {
                emptyMap()
            }
            usageStatsList = aggregated.values.toList()
        }

        val packageTimeMap = mutableMapOf<String, Long>()
        for (stats in usageStatsList) {
            val pkg = stats.packageName ?: continue
            val time = stats.totalTimeInForeground
            if (time > 0L) {
                packageTimeMap[pkg] = (packageTimeMap[pkg] ?: 0L) + time
            }
        }

        val appInfos = mutableListOf<AppUsageInfo>()
        var totalDistractionMillis = 0L

        for ((pkg, pair) in launcherAppMap) {
            val timeMs = packageTimeMap[pkg] ?: 0L
            val mins = (timeMs / 60_000L).toInt()
            val displayMins = if (mins == 0 && timeMs >= 20_000L) 1 else mins
            val category = categorizePackage(pkg, pair.first)

            appInfos.add(
                AppUsageInfo(
                    packageName = pkg,
                    appName = pair.first,
                    usageMinutes = displayMins,
                    category = category,
                    icon = pair.second
                )
            )
            totalDistractionMillis += timeMs
        }

        for ((pkg, timeMs) in packageTimeMap) {
            if (pkg == selfPkg || launcherAppMap.containsKey(pkg)) continue
            if (timeMs < 30_000L) continue

            val appInfo = try {
                pm.getApplicationInfo(pkg, 0)
            } catch (_: Exception) {
                null
            } ?: continue

            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            if (isSystem && pm.getLaunchIntentForPackage(pkg) == null) continue

            val label = try {
                pm.getApplicationLabel(appInfo).toString()
            } catch (_: Exception) {
                pkg.substringAfterLast('.')
            }

            val icon = try {
                pm.getApplicationIcon(appInfo)
            } catch (_: Exception) {
                null
            }

            val mins = (timeMs / 60_000L).toInt()
            val displayMins = if (mins == 0 && timeMs >= 20_000L) 1 else mins

            appInfos.add(
                AppUsageInfo(
                    packageName = pkg,
                    appName = label,
                    usageMinutes = displayMins,
                    category = categorizePackage(pkg, label),
                    icon = icon
                )
            )
            totalDistractionMillis += timeMs
        }

        val sortedApps = appInfos.sortedWith(
            compareByDescending<AppUsageInfo> { it.usageMinutes }
                .thenBy { it.appName.lowercase() }
        )

        val totalDistractionMins = (totalDistractionMillis / 60_000L).toInt()

        val appsWithPercent = sortedApps.map {
            val pct = if (totalDistractionMins > 0 && it.usageMinutes > 0) {
                ((it.usageMinutes.toFloat() / totalDistractionMins.toFloat()) * 100).toInt()
            } else 0
            it.copy(percent = pct)
        }

        val categoryBreakdown = mutableMapOf<String, Int>()
        for (app in appsWithPercent.filter { it.usageMinutes > 0 }) {
            categoryBreakdown[app.category] = (categoryBreakdown[app.category] ?: 0) + app.usageMinutes
        }

        val topCategory = categoryBreakdown.maxByOrNull { it.value }?.key ?: "Social Media"

        return DistractionUsageSummary(
            totalScreenMinutes = totalDistractionMins,
            totalDistractionMinutes = totalDistractionMins,
            topCategory = topCategory,
            apps = appsWithPercent,
            categoryBreakdown = categoryBreakdown
        )
    }

    val SOCIAL_PACKAGES = setOf(
        "com.instagram.android",
        "com.snapchat.android",
        "com.twitter.android",
        "com.zhiliaoapp.musically",
        "com.facebook.katana",
        "com.reddit.frontpage",
        "com.facebook.orca",
        "com.linkedin.android",
        "com.pinterest"
    )

    val MESSAGING_PACKAGES = setOf(
        "com.whatsapp",
        "org.telegram.messenger",
        "com.discord",
        "org.thoughtcrime.securesms",
        "com.slack",
        "com.google.android.apps.messaging"
    )

    val STREAMING_PACKAGES = setOf(
        "com.google.android.youtube",
        "com.netflix.mediaclient",
        "in.startv.hotstar",
        "com.amazon.avod.thirdpartyclient",
        "com.spotify.music",
        "tv.twitch.android.app",
        "org.videolan.vlc"
    )

    val BROWSING_PACKAGES = setOf(
        "com.google.android.googlequicksearchbox",
        "com.android.chrome",
        "org.mozilla.firefox",
        "com.microsoft.emmx",
        "com.opera.browser"
    )

    val PRODUCTIVITY_PACKAGES = setOf(
        "com.openai.chatgpt",
        "com.anthropic.claude",
        "com.microsoft.office.word",
        "com.google.android.apps.docs",
        "com.notion.id",
        "com.todoist"
    )

    fun getApplicationCategory(context: Context, pkg: String): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val appInfo = context.packageManager.getApplicationInfo(pkg, 0)
                return when (appInfo.category) {
                    ApplicationInfo.CATEGORY_GAME -> "Games"
                    ApplicationInfo.CATEGORY_AUDIO, ApplicationInfo.CATEGORY_VIDEO -> "Video & Streaming"
                    ApplicationInfo.CATEGORY_SOCIAL -> "Social Media"
                    ApplicationInfo.CATEGORY_NEWS -> "News"
                    ApplicationInfo.CATEGORY_MAPS -> "Navigation"
                    ApplicationInfo.CATEGORY_PRODUCTIVITY -> "Productivity"
                    else -> null
                }
            } catch (_: Exception) {
                null
            }
        }
        return null
    }

    fun categorizePackage(pkg: String, name: String, context: Context? = null): String {
        if (SOCIAL_PACKAGES.contains(pkg)) return "Social Media"
        if (MESSAGING_PACKAGES.contains(pkg)) return "Messaging"
        if (STREAMING_PACKAGES.contains(pkg)) return "Video & Streaming"
        if (BROWSING_PACKAGES.contains(pkg)) return "Search & Browsing"
        if (PRODUCTIVITY_PACKAGES.contains(pkg)) return "Productivity"

        context?.let { ctx ->
            getApplicationCategory(ctx, pkg)?.let { return it }
        }

        val lower = "$pkg $name".lowercase()
        return when {
            lower.contains("instagram") || lower.contains("facebook") || lower.contains("twitter") ||
                    lower.contains("reddit") || lower.contains("tiktok") || lower.contains("snapchat") ||
                    lower.contains("threads") || lower.contains("linkedin") || lower.contains("x.com") -> "Social Media"

            lower.contains("youtube") || lower.contains("netflix") || lower.contains("prime") ||
                    lower.contains("disney") || lower.contains("twitch") || lower.contains("spotify") ||
                    lower.contains("video") || lower.contains("hotstar") || lower.contains("vlc") -> "Video & Streaming"

            lower.contains("whatsapp") || lower.contains("telegram") || lower.contains("discord") ||
                    lower.contains("signal") || lower.contains("slack") || lower.contains("messenger") ||
                    lower.contains("wechat") -> "Messaging"

            lower.contains("chrome") || lower.contains("firefox") || lower.contains("browser") ||
                    lower.contains("opera") || lower.contains("edge") || lower.contains("safari") ||
                    lower.contains("search") -> "Search & Browsing"

            lower.contains("game") || lower.contains("candy") || lower.contains("pubg") ||
                    lower.contains("clash") || lower.contains("chess") -> "Games"

            lower.contains("map") || lower.contains("navigate") || lower.contains("uber") ||
                    lower.contains("waze") -> "Navigation"

            lower.contains("news") || lower.contains("daily") || lower.contains("bbc") -> "News"

            lower.contains("note") || lower.contains("task") || lower.contains("focus") ||
                    lower.contains("office") || lower.contains("docs") || lower.contains("sheets") ||
                    lower.contains("ai") || lower.contains("chatgpt") -> "Productivity"

            else -> "Utilities"
        }
    }

    fun getForegroundPackageName(context: Context): String? {
        return try {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return null
            val endTime = System.currentTimeMillis()
            val startTime = endTime - 10000L
            val events = usageStatsManager.queryEvents(startTime, endTime)
            var lastForegroundPkg: String? = null
            var lastEventTime = 0L
            val event = android.app.usage.UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if (event.eventType == android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED && event.timeStamp >= lastEventTime) {
                    val pkg = event.packageName
                    if (pkg != null && !isSystemProcessOrDaemon(pkg)) {
                        lastForegroundPkg = pkg
                        lastEventTime = event.timeStamp
                    }
                }
            }
            if (lastForegroundPkg != null) {
                lastForegroundPkg
            } else {
                val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
                stats?.filter { it.packageName != null && !isSystemProcessOrDaemon(it.packageName) }
                    ?.maxByOrNull { it.lastTimeUsed }
                    ?.packageName
            }
        } catch (_: Exception) {
            null
        }
    }

    fun isBlockedDistractingApp(context: Context, packageName: String): Boolean {
        if (packageName == context.packageName) return false
        if (isSystemProcessOrDaemon(packageName)) return false

        val knownDistracting = setOf(
            "com.instagram.android",
            "com.google.android.youtube",
            "com.snapchat.android",
            "com.twitter.android",
            "com.facebook.katana",
            "com.facebook.lite",
            "com.facebook.orca",
            "com.reddit.frontpage",
            "com.zhiliaoapp.musically",
            "com.ss.android.ugc.trill",
            "com.dts.freefireth",
            "com.dts.freefiremax",
            "com.pubg.imobile",
            "com.tencent.ig",
            "com.netflix.mediaclient",
            "com.amazon.avod.thirdpartyclient",
            "tv.twitch.android.app",
            "com.pinterest",
            "com.threads.android",
            "com.valvesoftware.android.steam.community",
            "com.king.candycrushsaga",
            "com.supercell.clashofclans",
            "com.supercell.clashroyale",
            "com.supercell.brawlstars",
            "com.roblox.client"
        )
        if (knownDistracting.contains(packageName)) return true

        val category = categorizePackage(packageName, "", context)
        return category == "Social Media" || category == "Games" || category == "Video & Streaming"
    }

    fun getAppLabel(context: Context, packageName: String): String {
        getCachedLabel(packageName)?.let { return it }
        return try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            val label = pm.getApplicationLabel(info).toString()
            putCachedLabel(packageName, label)
            label
        } catch (_: Exception) {
            packageName.substringAfterLast('.')
        }
    }
}
