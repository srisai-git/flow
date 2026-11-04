package com.simats.focusflow.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.ui.components.CustomTopBar
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.theme.FocusFlowTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SessionDetailsScreen(onBack: () -> Unit = {}, onViewHistory: () -> Unit = {}) {
    val colors = FocusFlowTheme.colors
    val currentDateStr = remember {
        SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(Date()).uppercase()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .padding(top = 16.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Session details", currentDateStr, onBack)
        
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .background(colors.primary.copy(alpha = 0.14f), RoundedCornerShape(10.dp))
                .border(1.dp, colors.primary.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text("Logged focus record", color = colors.primaryLight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Deep Focus Session", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Recorded in your focus analytics and daily targets", fontSize = 13.sp, color = colors.textMuted)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Focused time", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("25 min", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = colors.textPrimary)
                        Text("planned", fontSize = 13.sp, color = colors.textMuted)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("25 min", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = colors.textPrimary)
                        Text("actual focused", fontSize = 13.sp, color = colors.textMuted)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total elapsed", fontSize = 13.sp, color = colors.textMuted)
                    Text("25:00", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Interruption time is excluded from focused minutes.", fontSize = 12.sp, color = colors.textMuted)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Focus rating", fontSize = 14.sp, color = colors.textMuted)
                    Text("5 / 5", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    repeat(5) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Session summary", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Focused effort completed with uninterrupted concentration.", fontSize = 13.sp, color = colors.textMuted, lineHeight = 19.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(28.dp))
        
        Button(
            onClick = onViewHistory,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .bounceClick(),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("View Session History", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
