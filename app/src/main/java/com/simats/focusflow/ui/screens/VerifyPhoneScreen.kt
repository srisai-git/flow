package com.simats.focusflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.ui.components.CustomTopBar

@Composable
fun VerifyPhoneScreen(onBack: () -> Unit = {}, onVerify: () -> Unit = {}, onChangePhone: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        CustomTopBar("Verify your phone", onBack = onBack)
        Spacer(modifier = Modifier.height(48.dp))
        Text("✉✓", color = Color(0xFF7158E2), fontSize = 32.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text("One small check.", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Enter the verification code sent to:", fontSize = 16.sp, color = Color(0xFF6B7280))
        Spacer(modifier = Modifier.height(16.dp))
        Text("+91 ••••• 43210", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
        
        Spacer(modifier = Modifier.height(32.dp))
        Text("6-digit verification code", fontSize = 14.sp, color = Color(0xFF6B7280))
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val code = listOf("4", "8", "2", "1", "6", "9")
            code.forEachIndexed { i, digit ->
                val borderColor = if (i == 5) Color(0xFF7158E2) else Color(0xFFE5E7EB)
                Box(
                    modifier = Modifier.weight(1f).aspectRatio(1f).padding(horizontal = 4.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(digit, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("Resend in 00:24", color = Color(0xFF7158E2), fontSize = 14.sp)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onVerify,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Verify phone", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        
        Spacer(modifier = Modifier.weight(1f))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("Change phone number", color = Color(0xFF7158E2), fontSize = 16.sp, modifier = Modifier.clickable { onChangePhone() })
        }
    }
}