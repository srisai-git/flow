package com.simats.focusflow.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Sealed class representing application screens and main bottom navigation items.
 */
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Dashboard : Screen("dashboard", "Home", Icons.Outlined.Home)
    object Tasks : Screen("tasks", "Tasks", Icons.Outlined.Checklist)
    object Timer : Screen("timer", "Timer", Icons.Outlined.Timer)
    object Habits : Screen("habits", "Habits", Icons.Outlined.Repeat)
    object Profile : Screen("profile", "Profile", Icons.Outlined.Person)

    companion object {
        val bottomNavScreens = listOf(Dashboard, Tasks, Timer, Habits, Profile)

        fun isMainScreen(route: String?): Boolean {
            return bottomNavScreens.any { it.route == route }
        }
    }
}
