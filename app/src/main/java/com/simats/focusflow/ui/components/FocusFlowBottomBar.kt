package com.simats.focusflow.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
    val haptic = LocalHapticFeedback.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        val hairlineBorder = Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.16f),
                Color.White.copy(alpha = 0.03f)
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xF211101C))
                .border(1.dp, hairlineBorder, RoundedCornerShape(28.dp))
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Screen.bottomNavScreens.forEach { screen ->
                val isSelected = currentDestination?.hierarchy?.any { dest ->
                    dest.route == screen.route ||
                    (screen.route == "home" && dest.route == "dashboard") ||
                    (screen == Screen.Activities && (dest.route == "tasks" || dest.route == "habits"))
                } == true

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.12f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = 0.65f,
                        stiffness = 500f
                    ),
                    label = "iconScale"
                )
                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFFC4B5FD) else Color(0xFF94A3B8),
                    label = "iconColor"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFFF1F1F5) else Color(0xFF94A3B8),
                    label = "textColor"
                )
                val pillAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0f,
                    animationSpec = spring(
                        dampingRatio = 0.75f,
                        stiffness = 400f
                    ),
                    label = "pillAlpha"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF8B5CF6).copy(alpha = 0.22f * pillAlpha),
                                    Color(0xFF6366F1).copy(alpha = 0.10f * pillAlpha)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            Color(0xFF8B5CF6).copy(alpha = 0.35f * pillAlpha),
                            RoundedCornerShape(24.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                        },
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
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = screen.title,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor,
                            maxLines = 1,
                            softWrap = false
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
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        val hairlineBorder = Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.16f),
                Color.White.copy(alpha = 0.03f)
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xF211101C))
                .border(1.dp, hairlineBorder, RoundedCornerShape(28.dp))
                .padding(horizontal = 4.dp, vertical = 4.dp),
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

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.12f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = 0.65f,
                        stiffness = 500f
                    ),
                    label = "iconScale"
                )
                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFFC4B5FD) else Color(0xFF94A3B8),
                    label = "iconColor"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFFF1F1F5) else Color(0xFF94A3B8),
                    label = "textColor"
                )
                val pillAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0f,
                    animationSpec = spring(
                        dampingRatio = 0.75f,
                        stiffness = 400f
                    ),
                    label = "pillAlpha"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF8B5CF6).copy(alpha = 0.22f * pillAlpha),
                                    Color(0xFF6366F1).copy(alpha = 0.10f * pillAlpha)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            Color(0xFF8B5CF6).copy(alpha = 0.35f * pillAlpha),
                            RoundedCornerShape(24.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNavigate(screen.route)
                        },
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
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = screen.title,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}
