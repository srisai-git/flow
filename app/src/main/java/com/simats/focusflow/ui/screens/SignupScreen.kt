package com.simats.focusflow.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.focusflow.R
import com.simats.focusflow.ui.components.CustomTopBar
import com.simats.focusflow.ui.components.SmartTextField
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.viewmodel.AuthUiState
import com.simats.focusflow.ui.viewmodel.AuthViewModel

/**
 * Modern minimalist Signup Screen for FocusFlow.
 * Connected to flow_backend via MVVM architecture and StateFlow.
 */
@Composable
fun SignupScreen(
    onBack: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onLogin: () -> Unit = onNavigateToLogin,
    onSignupSuccess: () -> Unit = {},
    onContinue: () -> Unit = onSignupSuccess,
    viewModel: AuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var agreedToTerms by remember { mutableStateOf(true) }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    // Local validation logic
    val isNameValid = name.isBlank() || name.trim().length >= 2
    val isEmailValid = email.isBlank() || (email.contains("@") && email.contains(".") && email.length >= 5)
    val isPasswordValid = password.isBlank() || password.length >= 6
    val passwordsMatch = confirmPassword.isBlank() || (password == confirmPassword)

    // React to success
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSignupSuccess()
            viewModel.resetState()
        }
    }

    // React to error messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { errorMsg ->
            snackbarHostState.showSnackbar(errorMsg)
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            CustomTopBar(
                title = "Create Account",
                onBack = onBack
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App Brand Logo & Name
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFF070510), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "FocusFlow Logo",
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "FocusFlow",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E24)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Join thousands of mindful achievers building deep focus habits.",
                fontSize = 15.sp,
                color = Color(0xFF6B7280),
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Error Banner Display
            if (uiState.errorMessage != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    color = Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚠️",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Text(
                            text = uiState.errorMessage ?: "",
                            fontSize = 13.sp,
                            color = Color(0xFF991B1B),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "✕",
                            fontSize = 14.sp,
                            color = Color(0xFF991B1B),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { viewModel.clearError() }
                                .padding(start = 8.dp)
                        )
                    }
                }
            }

            // Name Field
            SmartTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (uiState.errorMessage != null) viewModel.clearError()
                },
                label = "Full Name",
                isValid = isNameValid,
                errorMessage = if (hasAttemptedSubmit && name.isBlank()) {
                    "Full name is required"
                } else if (!isNameValid) {
                    "Name must be at least 2 characters long"
                } else null,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Email Field
            SmartTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (uiState.errorMessage != null) viewModel.clearError()
                },
                label = "Email Address",
                isValid = isEmailValid,
                errorMessage = if (hasAttemptedSubmit && email.isBlank()) {
                    "Email address is required"
                } else if (!isEmailValid) {
                    "Please enter a valid email address"
                } else null,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Password Field
            Text(
                text = "Password",
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (uiState.errorMessage != null) viewModel.clearError()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp),
                shape = RoundedCornerShape(12.dp),
                isError = (!isPasswordValid) || (hasAttemptedSubmit && password.isBlank()),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = if (password.length >= 6) Color(0xFF10B981) else Color(0xFFE5E7EB),
                    focusedBorderColor = Color(0xFF7158E2),
                    errorBorderColor = Color(0xFFDC2626)
                ),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                trailingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        if (password.length >= 6) {
                            Text(
                                text = "✓",
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                        Text(
                            text = if (passwordVisible) "Hide" else "Show",
                            color = Color(0xFF7158E2),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                        )
                    }
                }
            )

            if (hasAttemptedSubmit && password.isBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Password is required",
                    fontSize = 12.sp,
                    color = Color(0xFFDC2626)
                )
            } else if (!isPasswordValid) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Password must be at least 6 characters",
                    fontSize = 12.sp,
                    color = Color(0xFFDC2626)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Confirm Password Field
            Text(
                text = "Confirm Password",
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    if (uiState.errorMessage != null) viewModel.clearError()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp),
                shape = RoundedCornerShape(12.dp),
                isError = (!passwordsMatch) || (hasAttemptedSubmit && confirmPassword.isBlank()),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = if (confirmPassword.isNotBlank() && passwordsMatch) Color(0xFF10B981) else Color(0xFFE5E7EB),
                    focusedBorderColor = Color(0xFF7158E2),
                    errorBorderColor = Color(0xFFDC2626)
                ),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        hasAttemptedSubmit = true
                        if (agreedToTerms) {
                            viewModel.onRegister(
                                name = name,
                                email = email,
                                password = password,
                                confirmPassword = confirmPassword
                            )
                        }
                    }
                ),
                singleLine = true,
                trailingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        if (confirmPassword.isNotBlank() && passwordsMatch) {
                            Text(
                                text = "✓",
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                        Text(
                            text = if (confirmPasswordVisible) "Hide" else "Show",
                            color = Color(0xFF7158E2),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { confirmPasswordVisible = !confirmPasswordVisible }
                        )
                    }
                }
            )

            if (hasAttemptedSubmit && confirmPassword.isBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Please confirm your password",
                    fontSize = 12.sp,
                    color = Color(0xFFDC2626)
                )
            } else if (!passwordsMatch) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Passwords do not match",
                    fontSize = 12.sp,
                    color = Color(0xFFDC2626)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Terms & Privacy Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { agreedToTerms = !agreedToTerms }
            ) {
                Checkbox(
                    checked = agreedToTerms,
                    onCheckedChange = { agreedToTerms = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF7158E2),
                        uncheckedColor = Color(0xFF9CA3AF)
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "I agree to the Terms of Service & Privacy Policy",
                    fontSize = 13.sp,
                    color = Color(0xFF4B5563)
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Signup Action Button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    hasAttemptedSubmit = true
                    if (!agreedToTerms) {
                        viewModel.clearError()
                        return@Button
                    }
                    viewModel.onRegister(
                        name = name,
                        email = email,
                        password = password,
                        confirmPassword = confirmPassword
                    )
                },
                enabled = !uiState.isLoading && agreedToTerms,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .bounceClick(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7158E2),
                    disabledContainerColor = Color(0xFF7158E2).copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Create Account",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Navigation link back to Login
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Already have an account? ",
                        color = Color(0xFF6B7280),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Log In",
                        color = Color(0xFF7158E2),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable {
                                viewModel.clearError()
                                onNavigateToLogin()
                            }
                            .bounceClick()
                    )
                }
            }
        }
    }
}

/**
 * Backward compatibility alias for CreateAccountScreen.
 */
@Composable
fun CreateAccountScreen(
    onBack: () -> Unit = {},
    onContinue: () -> Unit = {},
    onLogin: () -> Unit = {}
) {
    SignupScreen(
        onBack = onBack,
        onSignupSuccess = onContinue,
        onNavigateToLogin = onLogin
    )
}

@Preview(showBackground = true)
@Composable
fun SignupScreenPreview() {
    SignupScreen()
}

