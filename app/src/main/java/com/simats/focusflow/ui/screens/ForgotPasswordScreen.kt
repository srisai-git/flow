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
import com.simats.focusflow.ui.theme.FocusFlowTheme
import com.simats.focusflow.ui.viewmodel.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit = {},
    onResetSuccess: (String) -> Unit = {},
    authViewModel: AuthViewModel = remember { AuthViewModel() }
) {
    val colors = FocusFlowTheme.colors
    var email by remember { mutableStateOf("") }
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
        containerColor = colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.background)
                .statusBarsPadding()
                .padding(top = 16.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            CustomTopBar("Reset Password", null, onBack)

            Spacer(modifier = Modifier.height(20.dp))
            Icon(Icons.Default.LockReset, contentDescription = null, tint = colors.primaryLight, modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.height(14.dp))
            Text("Create a new password", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Enter your registered email address and choose a strong new password to regain access.",
                fontSize = 14.sp,
                color = colors.textMuted,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

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
            Text("New Password", fontSize = 13.sp, color = colors.textMuted, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp),
                shape = RoundedCornerShape(12.dp),
                isError = !isPasswordValid,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = if (newPassword.length >= 6) Color(0xFF10B981) else colors.border,
                    focusedBorderColor = colors.primary,
                    errorBorderColor = Color(0xFFEF4444),
                    unfocusedContainerColor = colors.surface,
                    focusedContainerColor = colors.surface,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
                ),
                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                trailingIcon = {
                    Text(
                        text = if (newPasswordVisible) "Hide" else "Show",
                        color = colors.primaryLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { newPasswordVisible = !newPasswordVisible }.padding(end = 12.dp)
                    )
                }
            )
            if (!isPasswordValid) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Password must be at least 6 characters", fontSize = 12.sp, color = Color(0xFFEF4444))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Confirm Password Input
            Text("Confirm New Password", fontSize = 13.sp, color = colors.textMuted, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp),
                shape = RoundedCornerShape(12.dp),
                isError = !passwordsMatch,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = if (confirmPassword.isNotBlank() && passwordsMatch) Color(0xFF10B981) else colors.border,
                    focusedBorderColor = colors.primary,
                    errorBorderColor = Color(0xFFEF4444),
                    unfocusedContainerColor = colors.surface,
                    focusedContainerColor = colors.surface,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
                ),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                trailingIcon = {
                    Text(
                        text = if (confirmPasswordVisible) "Hide" else "Show",
                        color = colors.primaryLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { confirmPasswordVisible = !confirmPasswordVisible }.padding(end = 12.dp)
                    )
                }
            )
            if (!passwordsMatch) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Passwords do not match", fontSize = 12.sp, color = Color(0xFFEF4444))
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
                    .height(52.dp)
                    .bounceClick(),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Reset Password", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = "Back to Login",
                    color = colors.primaryLight,
                    fontSize = 15.sp,
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
