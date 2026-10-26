package com.simats.focusflow.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.R
import com.simats.focusflow.ui.components.CustomTopBar

import com.simats.focusflow.ui.components.SmartTextField
import com.simats.focusflow.ui.components.bounceClick

@Composable
fun LoginScreen(
    onBack: () -> Unit = {},
    onNavigateToSetup: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    onCreateAccount: () -> Unit = {}
) {
    var email by remember { mutableStateOf("sri.sai@example.com") }
    var password by remember { mutableStateOf("password123") }

    val isEmailValid = email.isBlank() || (email.contains("@") && email.contains(".")) || email.length >= 10
    val isPasswordValid = password.isBlank() || password.length >= 6

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        CustomTopBar(
            title = "Welcome back",
            onBack = onBack
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF070510), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Flow",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E24)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Continue your routine, one thoughtful session at a time.",
            fontSize = 16.sp,
            color = Color(0xFF6B7280),
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        SmartTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email Address",
            isValid = isEmailValid,
            errorMessage = if (!isEmailValid) "Please enter a valid email address" else null
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(text = "Password", fontSize = 14.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp),
            shape = RoundedCornerShape(12.dp),
            isError = !isPasswordValid,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = if (password.length >= 6) Color(0xFF10B981) else Color(0xFFE5E7EB),
                focusedBorderColor = Color(0xFF7158E2),
                errorBorderColor = Color(0xFFDC2626)
            ),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            trailingIcon = {
                if (password.length >= 6) {
                    Text("✓", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp))
                }
            }
        )
        if (!isPasswordValid) {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Password must be at least 6 characters", fontSize = 12.sp, color = Color(0xFFDC2626))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Forgot password?",
                color = Color(0xFF7158E2),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onForgotPassword() }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = {
                if (isEmailValid && isPasswordValid) {
                    onNavigateToSetup()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .bounceClick(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Log In", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = "New to Flow? Create an account",
                color = Color(0xFF7158E2),
                fontSize = 14.sp,
                modifier = Modifier.clickable { onCreateAccount() }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(onNavigateToSetup = {})
}