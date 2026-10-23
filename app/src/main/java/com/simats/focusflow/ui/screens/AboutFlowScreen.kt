package com.simats.focusflow.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.R
import com.simats.focusflow.ui.components.CustomTopBar

@Composable
fun AboutFlowScreen(onBack: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("About Flow", null, onBack)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFF070510), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(50.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Flow", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Text("Version 1.0.0", fontSize = 14.sp, color = Color(0xFF7158E2), fontWeight = FontWeight.SemiBold)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column {
                Text("A little space for focus", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Flow helps you build a mindful, distraction-free productivity routine. Powered by Groot AI for personalized focus insights.",
                    fontSize = 15.sp,
                    color = Color(0xFF4B5563),
                    lineHeight = 22.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column {
                Text("Features", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(12.dp))
                Text("• Groot AI Assistant for smart focus analysis", fontSize = 14.sp, color = Color(0xFF6B7280))
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Gentle reminders & session history", fontSize = 14.sp, color = Color(0xFF6B7280))
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Distraction tracking & progress insights", fontSize = 14.sp, color = Color(0xFF6B7280))
            }
        }
    }
}