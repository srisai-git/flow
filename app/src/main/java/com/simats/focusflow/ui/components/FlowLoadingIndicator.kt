package com.simats.focusflow.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.simats.focusflow.ui.theme.FocusFlowTheme

/**
 * Branded Minimalist Loading Indicator for FocusFlow.
 * Renders an elegant rotating violet gradient arc with a soft pulsating center glow,
 * replacing standard material progress spinners with the app's refined aesthetic.
 */
@Composable
fun FlowLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    strokeWidth: Dp = 3.5.dp,
    color: Color = FocusFlowTheme.colors.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "FlowLoadingTransition")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val radius = (size.toPx() - strokePx) / 2f
            val centerOffset = center

            // Subtle background ring
            drawCircle(
                color = color.copy(alpha = 0.12f),
                radius = radius,
                center = centerOffset,
                style = Stroke(width = strokePx)
            )

            // Animated sweeping violet gradient arc
            val gradientBrush = Brush.sweepGradient(
                colors = listOf(
                    color.copy(alpha = 0.05f),
                    color.copy(alpha = pulseAlpha),
                    color
                )
            )

            drawArc(
                brush = gradientBrush,
                startAngle = rotation,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(strokePx / 2f, strokePx / 2f),
                size = androidx.compose.ui.geometry.Size(size.toPx() - strokePx, size.toPx() - strokePx),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }
    }
}

