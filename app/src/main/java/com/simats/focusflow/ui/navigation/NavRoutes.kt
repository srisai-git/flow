package com.simats.focusflow.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Sealed class representing application screens and main bottom navigation items.
 * Configured with exactly 4 primary root tabs (Requirement 11):
 * 1. Home (Dashboard)
 * 2. Activities (Tasks + Habits)
 * 3. Insights
 * 4. Profile
 */
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Dashboard : Screen("dashboard", "Home", Icons.Outlined.Home)
    object Activities : Screen("activities", "Activities", Icons.Outlined.Checklist)
    object Timer : Screen("timer", "Timer", Icons.Outlined.Timer)
    object Insights : Screen("insights", "Insights", Icons.Outlined.Insights)
    object Profile : Screen("profile", "Profile", Icons.Outlined.Person)

    // Legacy screen objects maintained for direct deep-link and programmatic routing
    object Tasks : Screen("tasks", "Tasks", Icons.Outlined.Checklist)
    object Habits : Screen("habits", "Habits", Icons.Outlined.Repeat)

    companion object {
        val Home: Screen get() = Dashboard

        val bottomNavScreens: List<Screen> by lazy {
            listOfNotNull(Dashboard, Activities, Timer, Insights, Profile)
        }

        fun isMainScreen(route: String?): Boolean {
            if (route == null) return false
            val mainRoutes = listOf(
                Screen.Home.route,
                Screen.Timer.route,
                Screen.Habits.route,
                Screen.Insights.route,
                Screen.Profile.route,
                Screen.Dashboard.route,
                Screen.Activities.route,
                Screen.Tasks.route
            )
            return mainRoutes.any { mainRoute ->
                route.startsWith(mainRoute)
            }
        }
    }
}
