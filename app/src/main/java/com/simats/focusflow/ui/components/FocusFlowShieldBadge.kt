package com.simats.focusflow.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.simats.focusflow.R

private fun createCrestShieldPath(w: Float, h: Float): Path {
    val path = Path()
    val r = w * 0.12f
    path.moveTo(r, 0f)
    path.lineTo(w - r, 0f)
    path.quadraticTo(w, 0f, w, r)
    path.lineTo(w, h * 0.44f)
    path.cubicTo(w, h * 0.68f, w * 0.62f, h * 0.90f, w * 0.5f, h)
    path.cubicTo(w * 0.38f, h * 0.90f, 0f, h * 0.68f, 0f, h * 0.44f)
    path.lineTo(0f, r)
    path.quadraticTo(0f, 0f, r, 0f)
    path.close()
    return path
}

@Composable
fun FocusFlowShieldBadge(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val shieldResId = remember(context) {
        val id = context.resources.getIdentifier("ic_focus_shield_3d", "drawable", context.packageName)
        if (id != 0) id else null
    }

    val infiniteTransition = rememberInfiniteTransition(label = "ShieldShimmer")
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = -0.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerProgress"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (shieldResId != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF8B5CF6).copy(alpha = 0.20f),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = size.minDimension * 0.70f
                            )
                        )
                    }
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF13111C)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = shieldResId),
                    contentDescription = "FocusFlow Shield",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                if (w <= 0f || h <= 0f) return@Canvas

                val outerPath = createCrestShieldPath(w, h)

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF8B5CF6).copy(alpha = 0.20f), Color.Transparent),
                        center = Offset(w * 0.5f, h * 0.5f),
                        radius = w * 0.75f
                    ),
                    center = Offset(w * 0.5f, h * 0.5f),
                    radius = w * 0.75f
                )

                drawPath(
                    path = outerPath,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFE9D5FF),
                            Color(0xFFC084FC),
                            Color(0xFF8B5CF6),
                            Color(0xFF6D28D9),
                            Color(0xFF3B0764)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    )
                )

                val inset = w * 0.075f
                val innerW = w - inset * 2
                val innerH = h - inset * 2
                val innerPath = createCrestShieldPath(innerW, innerH)

                withTransform({
                    translate(left = inset, top = inset)
                }) {
                    clipPath(innerPath) {
                        drawPath(
                            path = innerPath,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF5B21B6),
                                    Color(0xFF4C1D95),
                                    Color(0xFF3B0764)
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(innerW, innerH)
                            )
                        )

                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF1E1B4B).copy(alpha = 0.40f)
                                ),
                                start = Offset(innerW * 0.45f, 0f),
                                end = Offset(innerW, 0f)
                            ),
                            topLeft = Offset(innerW * 0.45f, 0f),
                            size = Size(innerW * 0.55f, innerH)
                        )

                        drawPath(
                            path = innerPath,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1E1B4B).copy(alpha = 0.65f),
                                    Color.Transparent
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(innerW * 0.45f, innerH * 0.45f)
                            )
                        )

                        val shimmerWidth = innerW * 0.45f
                        val shimmerCenter = (innerW + shimmerWidth * 2) * shimmerProgress - shimmerWidth
                        val shimmerBrush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.12f),
                                Color.White.copy(alpha = 0.32f),
                                Color.White.copy(alpha = 0.12f),
                                Color.Transparent
                            ),
                            start = Offset(shimmerCenter - shimmerWidth * 0.5f, 0f),
                            end = Offset(shimmerCenter + shimmerWidth * 0.5f, innerH)
                        )
                        drawRect(brush = shimmerBrush)
                    }
                }

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFEDE9FE).copy(alpha = 0.30f), Color.Transparent),
                        center = Offset(w * 0.5f, h * 0.44f),
                        radius = w * 0.28f
                    ),
                    center = Offset(w * 0.5f, h * 0.44f),
                    radius = w * 0.28f
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize(0.50f)
                    .align(BiasAlignment(0f, -0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color(0xFF1E1B4B)),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(x = 1.2.dp, y = 1.5.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "FocusFlow Shield Logo",
                    colorFilter = ColorFilter.tint(Color.White),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
