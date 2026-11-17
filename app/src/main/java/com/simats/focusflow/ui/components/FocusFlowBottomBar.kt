package com.simats.focusflow.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.simats.focusflow.ui.navigation.Screen
import com.simats.focusflow.ui.theme.FocusFlowTheme

@Composable
fun FocusFlowBottomBar(
    navController: NavController,
    modifier: Modifier = Modifier,
    onNavigate: ((String) -> Unit)? = null
) {
    val colors = FocusFlowTheme.colors
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(24.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Screen.bottomNavScreens.forEach { screen ->
                val isSelected = currentDestination?.hierarchy?.any { dest ->
                    dest.route == screen.route ||
                    (screen.route == "home" && dest.route == "dashboard") ||
                    (screen == Screen.Activities && (dest.route == "tasks" || dest.route == "habits"))
                } == true

                val scale by animateFloatAsState(targetValue = if (isSelected) 1.08f else 1f, label = "iconScale")
                val iconColor by animateColorAsState(targetValue = if (isSelected) colors.primary else colors.textMuted, label = "iconColor")
                val textColor by animateColorAsState(targetValue = if (isSelected) colors.primary else colors.textMuted, label = "textColor")
                val pillAlpha by animateColorAsState(
                    targetValue = if (isSelected) colors.primary.copy(alpha = 0.16f) else Color.Transparent,
                    label = "pillAlpha"
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(pillAlpha)
                        .clickable {
                            if (onNavigate != null) {
                                onNavigate(screen.route)
                            } else {
                                val target = if (screen.route == "dashboard") "home" else screen.route
                                val isHome = (target == "home")
                                navController.navigate(target) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = !isHome
                                    }
                                    launchSingleTop = true
                                    restoreState = !isHome
                                }
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.title,
                            tint = iconColor,
                            modifier = Modifier
                                .size(22.dp)
                                .scale(scale)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = screen.title,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FocusFlowBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FocusFlowTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(24.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Screen.bottomNavScreens.forEach { screen ->
                val isSelected = currentRoute != null && (
                    currentRoute == screen.route ||
                    (screen.route == "home" && currentRoute == "dashboard") ||
                    currentRoute.startsWith("${screen.route}/") ||
                    (screen == Screen.Activities && (currentRoute == "tasks" || currentRoute == "habits"))
                )
                val scale by animateFloatAsState(targetValue = if (isSelected) 1.08f else 1f, label = "iconScale")
                val iconColor by animateColorAsState(targetValue = if (isSelected) colors.primary else colors.textMuted, label = "iconColor")
                val textColor by animateColorAsState(targetValue = if (isSelected) colors.primary else colors.textMuted, label = "textColor")
                val pillAlpha by animateColorAsState(
                    targetValue = if (isSelected) colors.primary.copy(alpha = 0.16f) else Color.Transparent,
                    label = "pillAlpha"
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(pillAlpha)
                        .clickable {
                            onNavigate(screen.route)
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.title,
                            tint = iconColor,
                            modifier = Modifier
                                .size(22.dp)
                                .scale(scale)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = screen.title,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}
