package com.simats.focusflow.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    showBackIcon: Boolean = true
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showBackIcon && onBack != null) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onBack() },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Canvas(modifier = Modifier.size(24.dp)) {
                        val stroke = 2.dp.toPx()
                        val color = Color(0xFF1E1E24)
                        drawLine(color, Offset(size.width, size.height / 2), Offset(0f, size.height / 2), strokeWidth = stroke)
                        drawLine(color, Offset(0f, size.height / 2), Offset(size.width * 0.4f, size.height * 0.1f), strokeWidth = stroke)
                        drawLine(color, Offset(0f, size.height / 2), Offset(size.width * 0.4f, size.height * 0.9f), strokeWidth = stroke)
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E24)
            )
        }
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = if (showBackIcon && onBack != null) 40.dp else 0.dp)
            )
        }
    }
}