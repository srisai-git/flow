package com.simats.focusflow.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.simats.focusflow.R

private fun createShieldPath(w: Float, h: Float): Path {
    val path = Path()
    val r = w * 0.14f
    path.moveTo(r, 0f)
    path.lineTo(w - r, 0f)
    path.quadraticTo(w, 0f, w, r)
    path.lineTo(w, h * 0.52f)
    path.cubicTo(w, h * 0.76f, w * 0.65f, h * 0.94f, w * 0.5f, h)
    path.cubicTo(w * 0.35f, h * 0.94f, 0f, h * 0.76f, 0f, h * 0.52f)
    path.lineTo(0f, r)
    path.quadraticTo(0f, 0f, r, 0f)
    path.close()
    return path
}

@Composable
fun FocusFlowShieldBadge(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (w <= 0f || h <= 0f) return@Canvas

            val outerPath = createShieldPath(w, h)

            drawPath(
                path = outerPath,
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF8B5CF6).copy(alpha = 0.35f), Color.Transparent),
                    center = Offset(w * 0.5f, h * 0.5f),
                    radius = w * 0.75f
                )
            )

            drawPath(
                path = outerPath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFE9D5FF),
                        Color(0xFFC084FC),
                        Color(0xFF7C3AED),
                        Color(0xFF5B21B6)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(w, h)
                )
            )

            val inset = w * 0.07f
            val innerW = w - inset * 2
            val innerH = h - inset * 2
            val innerPath = createShieldPath(innerW, innerH)

            withTransform({
                translate(left = inset, top = inset)
            }) {
                drawPath(
                    path = innerPath,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFA855F7),
                            Color(0xFF8B5CF6),
                            Color(0xFF6D28D9),
                            Color(0xFF4C1D95)
                        ),
                        center = Offset(innerW * 0.45f, innerH * 0.35f),
                        radius = innerW * 0.85f
                    )
                )

                val sheenPath = Path().apply {
                    moveTo(innerW * 0.1f, 0f)
                    lineTo(innerW * 0.9f, 0f)
                    lineTo(innerW * 0.9f, innerH * 0.22f)
                    quadraticTo(innerW * 0.5f, innerH * 0.45f, innerW * 0.1f, innerH * 0.28f)
                    close()
                }
                drawPath(
                    path = sheenPath,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.32f),
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        start = Offset(innerW * 0.5f, 0f),
                        end = Offset(innerW * 0.5f, innerH * 0.45f)
                    )
                )
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFEDE9FE).copy(alpha = 0.5f), Color.Transparent),
                    center = Offset(w * 0.5f, h * 0.46f),
                    radius = w * 0.30f
                ),
                center = Offset(w * 0.5f, h * 0.46f),
                radius = w * 0.30f
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize(0.48f)
                .align(BiasAlignment(0f, -0.06f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "FocusFlow Shield Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
