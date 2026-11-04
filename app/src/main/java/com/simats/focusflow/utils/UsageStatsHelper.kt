package com.simats.focusflow.utils

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import java.util.Calendar

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val usageMinutes: Int,
    val category: String,
    val percent: Int = 0
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

    fun queryWeeklyDistractionStats(context: Context): DistractionUsageSummary {
        if (!hasUsageStatsPermission(context)) {
            return DistractionUsageSummary()
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return DistractionUsageSummary()

        val cal = Calendar.getInstance()
        val endTime = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, -7)
        val startTime = cal.timeInMillis

        val usageStatsList: List<UsageStats> = try {
            usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_WEEKLY,
                startTime,
                endTime
            ) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }

        val pm = context.packageManager
        val selfPkg = context.packageName

        // Aggregate by package
        val packageTimeMap = mutableMapOf<String, Long>()
        for (stats in usageStatsList) {
            val pkg = stats.packageName
            val time = stats.totalTimeInForeground
            if (time > 60_000L && pkg != selfPkg && !isSystemPackage(pkg)) {
                packageTimeMap[pkg] = (packageTimeMap[pkg] ?: 0L) + time
            }
        }

        val appInfos = mutableListOf<AppUsageInfo>()
        var totalDistractionMillis = 0L

        for ((pkg, timeMs) in packageTimeMap.entries.sortedByDescending { it.value }) {
            val appName = try {
                val appInfo = pm.getApplicationInfo(pkg, 0)
                pm.getApplicationLabel(appInfo).toString()
            } catch (_: PackageManager.NameNotFoundException) {
                pkg.substringAfterLast('.').replaceFirstChar { it.uppercase() }
            }

            val category = categorizePackage(pkg, appName)
            val mins = (timeMs / 60_000L).toInt()
            if (mins > 0) {
                appInfos.add(
                    AppUsageInfo(
                        packageName = pkg,
                        appName = appName,
                        usageMinutes = mins,
                        category = category
                    )
                )
                totalDistractionMillis += timeMs
            }
        }

        val totalDistractionMins = (totalDistractionMillis / 60_000L).toInt()

        // Calculate percentages
        val appsWithPercent = appInfos.map {
            val pct = if (totalDistractionMins > 0) {
                ((it.usageMinutes.toFloat() / totalDistractionMins.toFloat()) * 100).toInt()
            } else 0
            it.copy(percent = pct)
        }

        // Category breakdown
        val categoryBreakdown = mutableMapOf<String, Int>()
        for (app in appsWithPercent) {
            categoryBreakdown[app.category] = (categoryBreakdown[app.category] ?: 0) + app.usageMinutes
        }

        val topCategory = categoryBreakdown.maxByOrNull { it.value }?.key ?: "Social Media"

        return DistractionUsageSummary(
            totalScreenMinutes = totalDistractionMins,
            totalDistractionMinutes = totalDistractionMins,
            topCategory = topCategory,
            apps = appsWithPercent.take(10),
            categoryBreakdown = categoryBreakdown
        )
    }

    private fun isSystemPackage(pkg: String): Boolean {
        val lower = pkg.lowercase()
        return lower.startsWith("com.android.") ||
                lower.startsWith("com.google.android.inputmethod") ||
                lower.startsWith("com.google.android.gms") ||
                lower.contains("launcher") ||
                lower.contains("systemui") ||
                lower == "android"
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
