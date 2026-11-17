package com.simats.focusflow.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.ui.graphics.vector.ImageVector



sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : Screen("home", "Home", Icons.Outlined.Home)
    object Dashboard : Screen("home", "Home", Icons.Outlined.Home)
    object Activities : Screen("activities", "Activities", Icons.Outlined.Checklist)
    object Timer : Screen("timer", "Timer", Icons.Outlined.Timer)
    object Insights : Screen("insights", "Insights", Icons.Outlined.Insights)
    object Profile : Screen("profile", "Profile", Icons.Outlined.Person)

    object Tasks : Screen("tasks", "Tasks", Icons.Outlined.Checklist)
    object Habits : Screen("habits", "Habits", Icons.Outlined.Repeat)
    object Pomodoro : Screen("pomodoro_screen", "Pomodoro", Icons.Outlined.Timer)

    companion object {
        val bottomNavScreens: List<Screen> by lazy {
            listOfNotNull(Home, Activities, Timer, Insights, Profile)
        }

        fun isMainScreen(route: String?): Boolean {
            if (route == null) return false
            val mainRoutes = listOf(
                "home",
                "dashboard",
                "pomodoro_screen",
                Screen.Timer.route,
                Screen.Habits.route,
                Screen.Insights.route,
                Screen.Profile.route,
                Screen.Activities.route,
                Screen.Tasks.route
            )
            return mainRoutes.any { mainRoute ->
                route == mainRoute || route.startsWith("$mainRoute/")
            }
        }
    }
}
