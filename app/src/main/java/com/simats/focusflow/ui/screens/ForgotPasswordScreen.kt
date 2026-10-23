package com.simats.focusflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.ui.components.CustomTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(onBack: () -> Unit = {}, onSend: () -> Unit = {}, onBackToLogin: () -> Unit = {}) {
    var email by remember { mutableStateOf("sri.sai@example.com") }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        CustomTopBar("Forgot password?", onBack = onBack)
        Spacer(modifier = Modifier.height(48.dp))
        Text("✉", color = Color(0xFF7158E2), fontSize = 48.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text("A fresh start.", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Enter an email address or phone number to preview recovery instructions.", fontSize = 16.sp, color = Color(0xFF6B7280), lineHeight = 24.sp)
        
        Spacer(modifier = Modifier.height(32.dp))
        Text("Email or phone", fontSize = 14.sp, color = Color(0xFF6B7280))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE5E7EB), focusedBorderColor = Color(0xFF7158E2))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("✓ Email format looks good", fontSize = 12.sp, color = Color(0xFF10B981))
        
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onSend,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Send reset instructions", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFF9FAFB), RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(24.dp)) {
            Column {
                Text("The same response for everyone", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(12.dp))
                Text("If an account exists, reset instructions would be available. We never disclose whether an email is registered.", fontSize = 14.sp, color = Color(0xFF6B7280))
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("Back to Login", color = Color(0xFF7158E2), fontSize = 16.sp, modifier = Modifier.clickable { onBackToLogin() })
        }
    }
}