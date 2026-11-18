package com.simats.focusflow.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import com.simats.focusflow.ui.theme.FocusFlowTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun Modifier.bouncyClick(
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = 500f
        ),
        label = "bouncyScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(enabled) {
            if (!enabled) return@pointerInput
            awaitPointerEventScope {
                while (true) {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    try {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    } catch (_: Exception) {}
                    val up = waitForUpOrCancellation()
                    isPressed = false
                    if (up != null && onClick != null) {
                        onClick()
                    }
                }
            }
        }
}

fun Modifier.bounceClick(): Modifier = bouncyClick()

fun Modifier.noRippleClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
) = composed {
    this.clickable(
        enabled = enabled,
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

@Composable
fun AmbientMeshGlowHeader(
    modifier: Modifier = Modifier,
    height: Dp = 220.dp
) {
    val colors = FocusFlowTheme.colors
    val transition = rememberInfiniteTransition(label = "ambientMesh")
    val driftX by transition.animateFloat(
        initialValue = -25f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftX"
    )
    val driftY by transition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftY"
    )

    val glowColor1 = if (colors.isDark) Color(0xFF1A103C).copy(alpha = 0.18f) else Color(0xFF8B5CF6).copy(alpha = 0.08f)
    val glowColor2 = if (colors.isDark) Color(0xFF0D1B2A).copy(alpha = 0.18f) else Color(0xFF38BDF8).copy(alpha = 0.06f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val w = size.width
        val h = size.height

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    glowColor1,
                    Color.Transparent
                ),
                center = Offset(w * 0.25f + driftX, h * 0.4f + driftY),
                radius = w * 0.75f
            ),
            radius = w * 0.75f,
            center = Offset(w * 0.25f + driftX, h * 0.4f + driftY)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    glowColor2,
                    Color.Transparent
                ),
                center = Offset(w * 0.8f - driftX, h * 0.3f - driftY),
                radius = w * 0.65f
            ),
            radius = w * 0.65f,
            center = Offset(w * 0.8f - driftX, h * 0.3f - driftY)
        )
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = Color.Unspecified,
    borderBrush: Brush? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = FocusFlowTheme.colors
    val resolvedBg = if (backgroundColor != Color.Unspecified) backgroundColor else colors.surface
    val resolvedBorder = borderBrush ?: colors.cardBorderBrush
    Surface(
        modifier = modifier,
        shape = shape,
        color = resolvedBg,
        border = BorderStroke(1.dp, resolvedBorder)
    ) {
        Column(content = content)
    }
}

@Composable
fun rememberDarkShimmerBrush(): Brush {
    val colors = FocusFlowTheme.colors
    val transition = rememberInfiniteTransition(label = "darkShimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "darkShimmerTranslate"
    )

    val shimmerColors = if (colors.isDark) {
        listOf(
            Color(0xFF19162A),
            Color(0xFF282342),
            Color(0xFF19162A)
        )
    } else {
        listOf(
            Color(0xFFE2E8F0),
            Color(0xFFF1F5F9),
            Color(0xFFE2E8F0)
        )
    }

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 300f, translateAnim - 300f),
        end = Offset(translateAnim, translateAnim)
    )
}

@Composable
fun DarkShimmerSkeletonItem(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp)
) {
    val brush = rememberDarkShimmerBrush()
    Spacer(modifier = modifier.background(brush, shape))
}

@Composable
fun DashboardSkeleton() {
    val colors = FocusFlowTheme.colors
    val brush = rememberDarkShimmerBrush()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Spacer(modifier = Modifier.width(100.dp).height(14.dp).background(brush, RoundedCornerShape(6.dp)))
                Spacer(modifier = Modifier.height(6.dp))
                Spacer(modifier = Modifier.width(160.dp).height(24.dp).background(brush, RoundedCornerShape(6.dp)))
            }
            Spacer(modifier = Modifier.size(44.dp).background(brush, CircleShape))
        }

        Surface(
            modifier = Modifier.fillMaxWidth().height(150.dp),
            shape = RoundedCornerShape(22.dp),
            color = colors.surfaceVariant,
            border = BorderStroke(1.dp, colors.border)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Spacer(modifier = Modifier.width(70.dp).height(12.dp).background(brush, RoundedCornerShape(4.dp)))
                    Spacer(modifier = Modifier.height(10.dp))
                    Spacer(modifier = Modifier.width(120.dp).height(28.dp).background(brush, RoundedCornerShape(6.dp)))
                    Spacer(modifier = Modifier.height(12.dp))
                    Spacer(modifier = Modifier.width(90.dp).height(14.dp).background(brush, RoundedCornerShape(4.dp)))
                }
                Spacer(modifier = Modifier.size(100.dp).background(brush, CircleShape))
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth().height(80.dp),
            shape = RoundedCornerShape(18.dp),
            color = colors.surfaceVariant,
            border = BorderStroke(1.dp, colors.border)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.size(42.dp).background(brush, CircleShape))
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Spacer(modifier = Modifier.width(140.dp).height(16.dp).background(brush, RoundedCornerShape(4.dp)))
                    Spacer(modifier = Modifier.height(6.dp))
                    Spacer(modifier = Modifier.width(90.dp).height(12.dp).background(brush, RoundedCornerShape(4.dp)))
                }
            }
        }

        repeat(2) {
            Surface(
                modifier = Modifier.fillMaxWidth().height(68.dp),
                shape = RoundedCornerShape(16.dp),
                color = colors.surfaceVariant,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.size(22.dp).background(brush, RoundedCornerShape(6.dp)))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Spacer(modifier = Modifier.fillMaxWidth(0.6f).height(14.dp).background(brush, RoundedCornerShape(4.dp)))
                        Spacer(modifier = Modifier.height(6.dp))
                        Spacer(modifier = Modifier.fillMaxWidth(0.35f).height(10.dp).background(brush, RoundedCornerShape(4.dp)))
                    }
                }
            }
        }
    }
}

@Composable
fun TasksSkeleton() {
    val colors = FocusFlowTheme.colors
    val brush = rememberDarkShimmerBrush()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(4) {
            Surface(
                modifier = Modifier.fillMaxWidth().height(88.dp),
                shape = RoundedCornerShape(16.dp),
                color = colors.surfaceVariant,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.size(22.dp).background(brush, RoundedCornerShape(6.dp)))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Spacer(modifier = Modifier.fillMaxWidth(0.65f).height(15.dp).background(brush, RoundedCornerShape(4.dp)))
                        Spacer(modifier = Modifier.height(6.dp))
                        Spacer(modifier = Modifier.fillMaxWidth(0.4f).height(11.dp).background(brush, RoundedCornerShape(4.dp)))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Spacer(modifier = Modifier.width(50.dp).height(16.dp).background(brush, RoundedCornerShape(4.dp)))
                            Spacer(modifier = Modifier.width(42.dp).height(16.dp).background(brush, RoundedCornerShape(4.dp)))
                        }
                    }
                    Spacer(modifier = Modifier.size(24.dp).background(brush, CircleShape))
                }
            }
        }
    }
}

@Composable
fun InsightsSkeleton() {
    val colors = FocusFlowTheme.colors
    val brush = rememberDarkShimmerBrush()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(220.dp),
            shape = RoundedCornerShape(18.dp),
            color = colors.surfaceVariant,
            border = BorderStroke(1.dp, colors.border)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(18.dp)) {
                Spacer(modifier = Modifier.width(120.dp).height(24.dp).background(brush, RoundedCornerShape(6.dp)))
                Spacer(modifier = Modifier.height(6.dp))
                Spacer(modifier = Modifier.width(180.dp).height(12.dp).background(brush, RoundedCornerShape(4.dp)))
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.Bottom
                ) {
                    listOf(40, 75, 55, 90, 65, 80, 50).forEach { h ->
                        Spacer(
                            modifier = Modifier
                                .width(28.dp)
                                .height(h.dp)
                                .background(brush, RoundedCornerShape(6.dp))
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(2) {
                Surface(
                    modifier = Modifier.weight(1f).height(100.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surfaceVariant,
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Spacer(modifier = Modifier.size(28.dp).background(brush, CircleShape))
                        Spacer(modifier = Modifier.height(10.dp))
                        Spacer(modifier = Modifier.fillMaxWidth(0.6f).height(16.dp).background(brush, RoundedCornerShape(4.dp)))
                    }
                }
            }
        }
    }
}

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

@Composable
fun EmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector = androidx.compose.material.icons.Icons.Default.Search,
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
            Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(48.dp))
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
                Icon(imageVector = androidx.compose.material.icons.Icons.Default.CloudOff, contentDescription = null, tint = Color(0xFFB91C1C), modifier = Modifier.size(16.dp))
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
                    Icon(imageVector = androidx.compose.material.icons.Icons.Default.Check, contentDescription = "Valid", tint = Color(0xFF10B981), modifier = Modifier.padding(end = 12.dp).size(18.dp))
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
            .clip(RoundedCornerShape(12.dp))
            .noRippleClickable { onClick() }
            .bounceClick()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                .clip(RoundedCornerShape(6.dp))
                .border(2.dp, Color(0xFFE5E7EB), RoundedCornerShape(6.dp))
                .noRippleClickable { onCheckedChange(!isChecked) },
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
    val colors = FocusFlowTheme.colors
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
                        color = if (isSelected) colors.primary else colors.border,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(
                        color = if (isSelected) (if (colors.isDark) colors.primary.copy(alpha = 0.22f) else colors.primary.copy(alpha = 0.12f)) else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .noRippleClickable { onOptionSelected(option) }
                    .bounceClick(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    color = if (isSelected) (if (colors.isDark) Color.White else colors.primary) else colors.textMuted,
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
            .clip(RoundedCornerShape(12.dp))
            .noRippleClickable { onClick() }
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