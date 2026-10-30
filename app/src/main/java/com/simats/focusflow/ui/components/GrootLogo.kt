package com.simats.focusflow.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Stylized, minimalist vector icon inspired by Groot:
 * An organic geometric silhouette featuring a 4-point branch crown,
 * chiseled branch facets, and glowing violet almond eyes.
 * Zero emojis - pure vector geometric art.
 */
@Composable
fun GrootIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFF8B5CF6),
    eyeGlow: Color = Color(0xFFC4B5FD),
    barkColor: Color = Color(0xFF2E2C3D)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Outer Head / Branch Crown Path
        val headPath = Path().apply {
            // Start at bottom center chin
            moveTo(w * 0.5f, h * 0.92f)
            // Left jaw curve
            cubicTo(w * 0.28f, h * 0.92f, w * 0.20f, h * 0.75f, w * 0.20f, h * 0.55f)
            // Left temple rising to leftmost crown spike
            lineTo(w * 0.16f, h * 0.32f)
            lineTo(w * 0.28f, h * 0.38f)
            // Left-center crown spike
            lineTo(w * 0.38f, h * 0.18f)
            lineTo(w * 0.48f, h * 0.32f)
            // Right-center crown spike (taller)
            lineTo(w * 0.60f, h * 0.14f)
            lineTo(w * 0.70f, h * 0.34f)
            // Rightmost crown spike
            lineTo(w * 0.84f, h * 0.28f)
            // Right temple descending to right jaw
            lineTo(w * 0.80f, h * 0.55f)
            cubicTo(w * 0.80f, h * 0.75f, w * 0.72f, h * 0.92f, w * 0.5f, h * 0.92f)
            close()
        }

        // Draw solid head background silhouette
        drawPath(
            path = headPath,
            color = barkColor,
            style = Fill
        )

        // Draw head outline with primary tint
        drawPath(
            path = headPath,
            color = tint,
            style = Stroke(
                width = w * 0.065f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Wood grain facet line (top crown down between eyes)
        drawLine(
            color = tint.copy(alpha = 0.5f),
            start = Offset(w * 0.48f, h * 0.32f),
            end = Offset(w * 0.50f, h * 0.52f),
            strokeWidth = w * 0.045f,
            cap = StrokeCap.Round
        )

        // Left geometric branch facet line
        drawLine(
            color = tint.copy(alpha = 0.35f),
            start = Offset(w * 0.28f, h * 0.40f),
            end = Offset(w * 0.34f, h * 0.50f),
            strokeWidth = w * 0.035f,
            cap = StrokeCap.Round
        )

        // Right geometric branch facet line
        drawLine(
            color = tint.copy(alpha = 0.35f),
            start = Offset(w * 0.70f, h * 0.36f),
            end = Offset(w * 0.66f, h * 0.48f),
            strokeWidth = w * 0.035f,
            cap = StrokeCap.Round
        )

        // Subtle mouth bark slit
        drawLine(
            color = tint.copy(alpha = 0.6f),
            start = Offset(w * 0.42f, h * 0.78f),
            end = Offset(w * 0.58f, h * 0.78f),
            strokeWidth = w * 0.04f,
            cap = StrokeCap.Round
        )

        // Glowing Violet Almond Eyes
        val eyeWidth = w * 0.13f
        val eyeHeight = h * 0.14f
        val eyeY = h * 0.57f

        // Left Eye (outer glow + inner bright dot)
        drawOval(
            color = tint,
            topLeft = Offset(w * 0.32f - eyeWidth / 2f, eyeY - eyeHeight / 2f),
            size = Size(eyeWidth, eyeHeight)
        )
        drawCircle(
            color = eyeGlow,
            radius = eyeWidth * 0.32f,
            center = Offset(w * 0.32f, eyeY)
        )

        // Right Eye (outer glow + inner bright dot)
        drawOval(
            color = tint,
            topLeft = Offset(w * 0.68f - eyeWidth / 2f, eyeY - eyeHeight / 2f),
            size = Size(eyeWidth, eyeHeight)
        )
        drawCircle(
            color = eyeGlow,
            radius = eyeWidth * 0.32f,
            center = Offset(w * 0.68f, eyeY)
        )
    }
}

/**
 * Circular / rounded container avatar for Groot AI
 */
@Composable
fun GrootAvatar(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    containerColor: Color = Color(0xFF181722),
    borderColor: Color = Color(0xFF8B5CF6).copy(alpha = 0.4f),
    tint: Color = Color(0xFF8B5CF6),
    shape: androidx.compose.ui.graphics.Shape = CircleShape
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(containerColor)
            .border(1.dp, borderColor, shape),
        contentAlignment = Alignment.Center
    ) {
        GrootIcon(
            modifier = Modifier.size(size * 0.62f),
            tint = tint,
            eyeGlow = Color(0xFFDDD6FE),
            barkColor = Color(0xFF1F1D2B)
        )
    }
}

