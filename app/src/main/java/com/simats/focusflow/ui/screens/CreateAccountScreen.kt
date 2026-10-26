package com.simats.focusflow.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.R
import com.simats.focusflow.ui.components.CustomTopBar
import com.simats.focusflow.ui.components.SmartTextField
import com.simats.focusflow.ui.components.bounceClick

@Composable
fun CreateAccountScreen(
    onBack: () -> Unit = {},
    onContinue: () -> Unit = {},
    onLogin: () -> Unit = {}
) {
    var email by remember { mutableStateOf("sri.sai@example.com") }
    var password by remember { mutableStateOf("password123") }
    var passwordVisible by remember { mutableStateOf(false) }
    var agreed by remember { mutableStateOf(true) }

    val isEmailValid = email.isBlank() || (email.contains("@") && email.contains("."))
    val isPasswordValid = password.isBlank() || password.length >= 6

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomTopBar(title = "Create your account", onBack = onBack)
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.width(60.dp).height(4.dp).background(Color(0xFF1E1E24), RoundedCornerShape(2.dp)))
        }
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
            Text("Flow", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Start with your email address. Make room for better focus.", fontSize = 16.sp, color = Color(0xFF6B7280), lineHeight = 24.sp)
        
        Spacer(modifier = Modifier.height(32.dp))

        // Email Field
        SmartTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email Address",
            isValid = isEmailValid,
            errorMessage = if (!isEmailValid) "Please enter a valid email address" else null
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Password Field
        Text("Password", fontSize = 14.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
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
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            trailingIcon = {
                Text(
                    text = if (passwordVisible) "Hide" else "Show",
                    color = Color(0xFF7158E2),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { passwordVisible = !passwordVisible }.padding(end = 12.dp)
                )
            }
        )
        if (!isPasswordValid) {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Password must be at least 6 characters", fontSize = 12.sp, color = Color(0xFFDC2626))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { agreed = !agreed }) {
            Checkbox(checked = agreed, onCheckedChange = { agreed = it }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF7158E2)))
            Text("I agree to the Terms & Privacy Policy", fontSize = 14.sp, color = Color(0xFF6B7280))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (agreed && isEmailValid && isPasswordValid) {
                    onContinue()
                }
            },
            enabled = agreed && isEmailValid && isPasswordValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .bounceClick(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
        
        Spacer(modifier = Modifier.weight(1f))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("Already have an account? Log in", color = Color(0xFF7158E2), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { onLogin() })
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateAccountScreenPreview() {
    CreateAccountScreen()
}