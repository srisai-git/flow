package com.simats.focusflow.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Micro-interaction bounce modifier for buttons & cards
fun Modifier.bounceClick() = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "bounceScale")
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(isPressed) {
            awaitPointerEventScope {
                isPressed = if (isPressed) {
                    waitForUpOrCancellation()
                    false
                } else {
                    awaitFirstDown()
                    true
                }
            }
        }
}

// Skeleton Shimmer Loader Component
@Composable
fun ShimmerSkeletonItem(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp)
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFE5E7EB),
            Color(0xFFF3F4F6),
            Color(0xFFE5E7EB)
        ),
        start = Offset(translateAnim - 200f, translateAnim - 200f),
        end = Offset(translateAnim, translateAnim)
    )

    Spacer(
        modifier = modifier.background(brush, shape)
    )
}

// Empty State Layout Component
@Composable
fun EmptyStateCard(
    icon: String = "🔍",
    title: String = "No items found",
    subtitle: String = "Try adjusting your search or filters.",
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
            Spacer(modifier = Modifier.height(8.dp))
            Text(subtitle, fontSize = 14.sp, color = Color(0xFF6B7280), textAlign = TextAlign.Center)
            if (actionText != null && onAction != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp).bounceClick()
                ) {
                    Text(actionText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// Offline / Disconnected Banner Component
@Composable
fun OfflineBanner(
    isOffline: Boolean = false
) {
    if (isOffline) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFEF2F2))
                .padding(vertical = 8.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⚡", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Offline Mode — Working with local storage",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFB91C1C)
                )
            }
        }
    }
}

// Smart Form Field Component with A11y and Inline Validation
@Composable
fun SmartTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    errorMessage: String? = null,
    isValid: Boolean = true,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        Text(text = label, fontSize = 14.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp),
            shape = RoundedCornerShape(12.dp),
            isError = errorMessage != null,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = if (value.isNotBlank() && isValid) Color(0xFF10B981) else Color(0xFFE5E7EB),
                focusedBorderColor = Color(0xFF7158E2),
                errorBorderColor = Color(0xFFDC2626)
            ),
            trailingIcon = trailingIcon ?: {
                if (value.isNotBlank() && isValid) {
                    Text("✓", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp))
                }
            },
            singleLine = true
        )
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(errorMessage, fontSize = 12.sp, color = Color(0xFFDC2626))
        }
    }
}

@Composable
fun PriorityBadge(priority: String) {
    val (backgroundColor, textColor) = when (priority.lowercase()) {
        "high" -> Color(0xFFFCE8E8) to Color(0xFFE53E3E)
        "medium" -> Color(0xFFE1F0FF) to Color(0xFF3182CE)
        "low" -> Color(0xFFF3E8FF) to Color(0xFF805AD5)
        else -> Color(0xFFF3F4F6) to Color(0xFF4B5563)
    }

    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = priority,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
fun TaskItem(
    title: String,
    subtitle: String,
    priority: String,
    isChecked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {},
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .bounceClick()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                .border(2.dp, Color(0xFFE5E7EB), RoundedCornerShape(6.dp))
                .clickable { onCheckedChange(!isChecked) },
            contentAlignment = Alignment.Center
        ) {
            if (isChecked) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFF7158E2), RoundedCornerShape(3.dp))
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1E1E24)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color(0xFF6B7280)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        PriorityBadge(priority = priority)
    }
}

@Composable
fun SegmentedControl(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selectedOption
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Color(0xFF7158E2) else Color(0xFFE5E7EB),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(
                        color = if (isSelected) Color(0xFFF0EDFF) else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onOptionSelected(option) }
                    .bounceClick(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    color = if (isSelected) Color(0xFF7158E2) else Color(0xFF6B7280),
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun BottomNavBar(selectedItem: String, onNavigate: (String) -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(width = 1.dp, color = Color(0xFFF3F4F6))
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem("Home", selectedItem == "Home") { onNavigate("Home") }
        BottomNavItem("Insights", selectedItem == "Insights") { onNavigate("Insights") }
        BottomNavItem("Tasks", selectedItem == "Tasks") { onNavigate("Tasks") }
        BottomNavItem("Profile", selectedItem == "Profile") { onNavigate("Profile") }
    }
}

@Composable
fun BottomNavItem(label: String, isSelected: Boolean, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .clickable { onClick() }
            .bounceClick()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val color = if (isSelected) Color(0xFF7158E2) else Color(0xFF9CA3AF)
            val stroke = 2.dp.toPx()
            when (label) {
                "Home" -> {
                    drawLine(color, Offset(size.width/2, 0f), Offset(0f, size.height/2), strokeWidth = stroke)
                    drawLine(color, Offset(size.width/2, 0f), Offset(size.width, size.height/2), strokeWidth = stroke)
                    drawRect(color, Offset(size.width*0.2f, size.height/2), Size(size.width*0.6f, size.height/2), style = Stroke(stroke))
                }
                "Insights" -> {
                    drawLine(color, Offset(size.width*0.2f, size.height), Offset(size.width*0.2f, size.height*0.5f), strokeWidth = stroke)
                    drawLine(color, Offset(size.width*0.5f, size.height), Offset(size.width*0.5f, size.height*0.2f), strokeWidth = stroke)
                    drawLine(color, Offset(size.width*0.8f, size.height), Offset(size.width*0.8f, size.height*0.6f), strokeWidth = stroke)
                    drawLine(color, Offset(0f, size.height*0.6f), Offset(size.width*0.4f, size.height*0.1f), strokeWidth = stroke)
                    drawLine(color, Offset(size.width*0.4f, size.height*0.1f), Offset(size.width, size.height*0.4f), strokeWidth = stroke)
                }
                "Tasks" -> {
                    drawCircle(color, size.width/2, center, style = Stroke(stroke))
                    drawLine(color, Offset(size.width*0.3f, size.height*0.5f), Offset(size.width*0.45f, size.height*0.7f), strokeWidth = stroke)
                    drawLine(color, Offset(size.width*0.45f, size.height*0.7f), Offset(size.width*0.7f, size.height*0.3f), strokeWidth = stroke)
                }
                "Profile" -> {
                    drawCircle(color, size.width*0.25f, Offset(size.width/2, size.height*0.25f), style = Stroke(stroke))
                    drawArc(color, 180f, 180f, false, Offset(0f, size.height*0.5f), Size(size.width, size.height), style = Stroke(stroke))
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isSelected) Color(0xFF7158E2) else Color(0xFF9CA3AF),
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}