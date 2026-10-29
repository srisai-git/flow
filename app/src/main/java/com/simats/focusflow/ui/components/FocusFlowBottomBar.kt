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
import com.simats.focusflow.ui.navigation.Screen
import com.simats.focusflow.ui.theme.FocusFlowTheme

/**
 * Custom minimalist Bottom Navigation Bar for FocusFlow with sleek Material3 vector icons.
 * Adapts dynamically to Dark & Light themes.
 */
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
                val isSelected = currentRoute == screen.route
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
                            if (!isSelected) {
                                onNavigate(screen.route)
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
