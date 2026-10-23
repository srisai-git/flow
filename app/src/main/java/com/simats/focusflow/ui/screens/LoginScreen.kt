package com.simats.focusflow.ui.screens

import androidx.compose.foundation.Canvas
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

@Composable
fun LoginScreen(
    onBack: () -> Unit = {},
    onNavigateToSetup: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    onCreateAccount: () -> Unit = {}
) {
    var email by remember { mutableStateOf("sri.sai@example.com") }
    var password by remember { mutableStateOf("password123") }

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
        
        Text(text = "Phone or email", fontSize = 14.sp, color = Color(0xFF6B7280))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE5E7EB),
                focusedBorderColor = Color(0xFF7158E2)
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(text = "Password", fontSize = 14.sp, color = Color(0xFF6B7280))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE5E7EB),
                focusedBorderColor = Color(0xFF7158E2)
            ),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            trailingIcon = {
                Canvas(modifier = Modifier.size(24.dp).padding(end = 12.dp)) {
                    val stroke = 1.5.dp.toPx()
                    drawArc(
                        color = Color(0xFF6B7280),
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(0f, size.height * 0.25f),
                        size = Size(size.width, size.height * 0.5f),
                        style = Stroke(width = stroke)
                    )
                    drawArc(
                        color = Color(0xFF6B7280),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(0f, size.height * 0.25f),
                        size = Size(size.width, size.height * 0.5f),
                        style = Stroke(width = stroke)
                    )
                    drawCircle(
                        color = Color(0xFF6B7280),
                        radius = size.width * 0.2f,
                        center = center
                    )
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Forgot password?",
                color = Color(0xFF7158E2),
                fontSize = 14.sp,
                modifier = Modifier.clickable { onForgotPassword() }
            )
            Text(
                text = "Use OTP instead",
                color = Color(0xFF7158E2),
                fontSize = 14.sp,
                modifier = Modifier.clickable { onForgotPassword() }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onNavigateToSetup,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
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