package com.simats.focusflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.ui.components.CustomTopBar
import com.simats.focusflow.ui.components.SmartTextField
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.viewmodel.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit = {},
    onResetSuccess: (String) -> Unit = {},
    authViewModel: AuthViewModel = remember { AuthViewModel() }
) {
    var email by remember { mutableStateOf("sri.sai@example.com") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val uiState by authViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val isEmailValid = email.isBlank() || (email.contains("@") && email.contains("."))
    val isPasswordValid = newPassword.isBlank() || newPassword.length >= 6
    val passwordsMatch = confirmPassword.isBlank() || (newPassword == confirmPassword)

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            authViewModel.clearError()
        }
        uiState.successMessage?.let { success ->
            snackbarHostState.showSnackbar(success)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            CustomTopBar("Reset Password", null, onBack)

            Spacer(modifier = Modifier.height(24.dp))
            Icon(Icons.Default.LockReset, contentDescription = null, tint = Color(0xFF7158E2), modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Create a new password", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Enter your registered email address and choose a strong new password to regain access.",
                fontSize = 15.sp,
                color = Color(0xFF6B7280),
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Email Input
            SmartTextField(
                value = email,
                onValueChange = { email = it },
                label = "Registered Email Address",
                isValid = isEmailValid,
                errorMessage = if (!isEmailValid) "Please enter a valid email address" else null
            )

            Spacer(modifier = Modifier.height(20.dp))

            // New Password Input
            Text("New Password", fontSize = 14.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp),
                shape = RoundedCornerShape(12.dp),
                isError = !isPasswordValid,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = if (newPassword.length >= 6) Color(0xFF10B981) else Color(0xFFE5E7EB),
                    focusedBorderColor = Color(0xFF7158E2),
                    errorBorderColor = Color(0xFFDC2626)
                ),
                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                trailingIcon = {
                    Text(
                        text = if (newPasswordVisible) "Hide" else "Show",
                        color = Color(0xFF7158E2),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { newPasswordVisible = !newPasswordVisible }.padding(end = 12.dp)
                    )
                }
            )
            if (!isPasswordValid) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Password must be at least 6 characters", fontSize = 12.sp, color = Color(0xFFDC2626))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Confirm Password Input
            Text("Confirm New Password", fontSize = 14.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp),
                shape = RoundedCornerShape(12.dp),
                isError = !passwordsMatch,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = if (confirmPassword.isNotBlank() && passwordsMatch) Color(0xFF10B981) else Color(0xFFE5E7EB),
                    focusedBorderColor = Color(0xFF7158E2),
                    errorBorderColor = Color(0xFFDC2626)
                ),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                trailingIcon = {
                    Text(
                        text = if (confirmPasswordVisible) "Hide" else "Show",
                        color = Color(0xFF7158E2),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { confirmPasswordVisible = !confirmPasswordVisible }.padding(end = 12.dp)
                    )
                }
            )
            if (!passwordsMatch) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Passwords do not match", fontSize = 12.sp, color = Color(0xFFDC2626))
            } else if (confirmPassword.isNotBlank() && confirmPassword == newPassword) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Passwords match", fontSize = 12.sp, color = Color(0xFF10B981))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    authViewModel.resetPassword(
                        email = email,
                        newPassword = newPassword,
                        confirmPassword = confirmPassword,
                        onSuccess = { msg ->
                            onResetSuccess(msg)
                        }
                    )
                },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .bounceClick(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2)),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Reset Password", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Back to Login",
                    color = Color(0xFF7158E2),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onBack() }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordScreenPreview() {
    ForgotPasswordScreen()
}
