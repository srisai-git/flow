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
import java.util.Calendar

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val usageMinutes: Int,
    val category: String,
    val percent: Int = 0,
    val icon: Drawable? = null
)

data class DistractionUsageSummary(
    val totalScreenMinutes: Int = 0,
    val totalDistractionMinutes: Int = 0,
    val topCategory: String = "Social Media",
    val apps: List<AppUsageInfo> = emptyList(),
    val categoryBreakdown: Map<String, Int> = emptyMap()
)

object UsageStatsHelper {

    fun hasUsageStatsPermission(context: Context): Boolean {
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
        return mode == AppOpsManager.MODE_ALLOWED
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

    /**
     * Resolves all user-facing launcher applications (including user-installed Play Store apps)
     * using PackageManager.queryIntentActivities(MAIN / LAUNCHER).
     */
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

    /**
     * Queries device usage statistics strictly for TODAY (from 00:00:00 midnight to now).
     * Filters out non-launcher system processes and prevents interval double-counting.
     */
    fun queryTodayDistractionStats(context: Context): DistractionUsageSummary {
        val pm = context.packageManager
        val selfPkg = context.packageName

        // 1. Resolve ONLY user-facing launcher applications (Play Store & user launcher apps)
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
            // Strictly exclude FocusFlow itself from distraction stats
            if (pkg == selfPkg || launcherAppMap.containsKey(pkg)) continue

            val label = try {
                resolveInfo.loadLabel(pm).toString()
            } catch (_: Exception) {
                pkg.substringAfterLast('.')
            }

            val icon = try {
                resolveInfo.loadIcon(pm)
            } catch (_: Exception) {
                null
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

        // Today boundary: strictly 00:00:00 midnight to System.currentTimeMillis()
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = cal.timeInMillis
        val currentTime = System.currentTimeMillis()

        val usageStatsList: List<UsageStats> = try {
            usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startOfDay,
                currentTime
            ) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }

        // Aggregate totalTimeInForeground strictly for today without prior-day overlap or double counting
        val packageTimeMap = mutableMapOf<String, Long>()
        for (stats in usageStatsList) {
            val pkg = stats.packageName ?: continue
            if (pkg == selfPkg || !launcherAppMap.containsKey(pkg)) continue
            // Ensure app was actually active today
            if (stats.lastTimeUsed < startOfDay) continue

            val time = stats.totalTimeInForeground
            if (time > 0L) {
                // Avoid double-counting overlapping daily interval buckets
                val existing = packageTimeMap[pkg] ?: 0L
                packageTimeMap[pkg] = maxOf(existing, time)
            }
        }

        // Fallback to queryAndAggregateUsageStats if queryUsageStats was empty
        if (packageTimeMap.isEmpty()) {
            try {
                val aggregated = usageStatsManager.queryAndAggregateUsageStats(startOfDay, currentTime)
                for ((pkg, stats) in aggregated) {
                    if (pkg == selfPkg || !launcherAppMap.containsKey(pkg)) continue
                    if (stats.lastTimeUsed >= startOfDay && stats.totalTimeInForeground > 0L) {
                        packageTimeMap[pkg] = stats.totalTimeInForeground
                    }
                }
            } catch (_: Exception) {}
        }

        val appInfos = mutableListOf<AppUsageInfo>()
        var totalDistractionMillis = 0L

        // Strictly include only launcher apps that had usage today
        for ((pkg, pair) in launcherAppMap) {
            val timeMs = packageTimeMap[pkg] ?: 0L
            val mins = (timeMs / 60_000L).toInt()
            val displayMins = if (mins == 0 && timeMs >= 15_000L) 1 else mins
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
            totalDistractionMillis += (displayMins * 60_000L)
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

        // 1. Discover all user-facing launcher applications (guarantees Play Store apps are included)
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

        // Map packageName -> Pair(appName, appIcon)
        val launcherAppMap = mutableMapOf<String, Pair<String, Drawable?>>()
        for (resolveInfo in resolveInfos) {
            val pkg = resolveInfo.activityInfo?.packageName ?: continue
            if (pkg == selfPkg || launcherAppMap.containsKey(pkg)) continue

            val label = try {
                resolveInfo.loadLabel(pm).toString()
            } catch (_: Exception) {
                pkg.substringAfterLast('.')
            }

            val icon = try {
                resolveInfo.loadIcon(pm)
            } catch (_: Exception) {
                null
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

        // Aggregate totalTimeInForeground by package
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

        // Cross-reference all discovered launcher apps against UsageStats
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

        // Include any remaining package that had usage and is a launchable user app
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

        // Sort: Apps with recorded usage time first descending, then alphabetical
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

    private fun categorizePackage(pkg: String, name: String): String {
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
                    lower.contains("opera") || lower.contains("edge") || lower.contains("safari") -> "Browsing"

            lower.contains("game") || lower.contains("candy") || lower.contains("pubg") ||
                    lower.contains("clash") || lower.contains("chess") -> "Games"

            else -> "Other"
        }
    }
}
