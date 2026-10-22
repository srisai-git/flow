package com.simats.focusflow.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
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
                    .height(40.dp)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Color(0xFF7158E2) else Color(0xFFE5E7EB),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(
                        color = if (isSelected) Color(0xFFF0EDFF) else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onOptionSelected(option) },
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
            .padding(vertical = 12.dp),
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
            .clickable { onClick() }
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