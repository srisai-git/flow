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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.focusflow.R
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.ui.components.CustomTopBar
import com.simats.focusflow.ui.components.SmartTextField
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.viewmodel.AuthUiState
import com.simats.focusflow.ui.viewmodel.AuthViewModel

/**
 * Modern minimalist Login Screen for FocusFlow.
 * Connected to flow_backend via MVVM architecture and StateFlow.
 */
@Composable
fun LoginScreen(
    onBack: () -> Unit = {},
    onNavigateToSetup: () -> Unit = {},
    onLoginSuccess: () -> Unit = onNavigateToSetup,
    onForgotPassword: () -> Unit = {},
    onNavigateToSignup: () -> Unit = {},
    onCreateAccount: () -> Unit = onNavigateToSignup,
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.initSessionManager(SessionManager.getInstance(context))
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    // Local validation helpers
    val isEmailFormatValid = email.isBlank() || (email.contains("@") && email.contains(".") && email.length >= 5)
    val isPasswordLengthValid = password.isBlank() || password.length >= 6

    // React to success
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onLoginSuccess()
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
                title = "Welcome back",
                onBack = onBack
            )

            Spacer(modifier = Modifier.height(28.dp))

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

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Continue your routine, one thoughtful session at a time.",
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
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.padding(end = 10.dp).size(18.dp))
                        Text(
                            text = uiState.errorMessage ?: "",
                            fontSize = 13.sp,
                            color = Color(0xFF991B1B),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF991B1B), modifier = Modifier.clickable { viewModel.clearError() }.padding(start = 8.dp).size(16.dp))
                    }
                }
            }

            // Email Field
            SmartTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (uiState.errorMessage != null) viewModel.clearError()
                },
                label = "Email Address",
                isValid = isEmailFormatValid,
                errorMessage = if (hasAttemptedSubmit && email.isBlank()) {
                    "Email address is required"
                } else if (!isEmailFormatValid) {
                    "Please enter a valid email address"
                } else null,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

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
                isError = (!isPasswordLengthValid) || (hasAttemptedSubmit && password.isBlank()),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = if (password.length >= 6) Color(0xFF10B981) else Color(0xFFE5E7EB),
                    focusedBorderColor = Color(0xFF7158E2),
                    errorBorderColor = Color(0xFFDC2626)
                ),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        hasAttemptedSubmit = true
                        viewModel.onLogin(email = email, password = password)
                    }
                ),
                singleLine = true,
                trailingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        if (password.length >= 6) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.padding(end = 8.dp).size(16.dp))
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
            } else if (!isPasswordLengthValid) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Password must be at least 6 characters",
                    fontSize = 12.sp,
                    color = Color(0xFFDC2626)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Forgot Password Link
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

            Spacer(modifier = Modifier.height(30.dp))

            // Login Action Button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    hasAttemptedSubmit = true
                    viewModel.onLogin(email = email, password = password)
                },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .bounceClick(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7158E2),
                    disabledContainerColor = Color(0xFF7158E2).copy(alpha = 0.6f)
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
                        text = "Log In",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Navigation link to Signup
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
                        text = "New to Flow? ",
                        color = Color(0xFF6B7280),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Create an account",
                        color = Color(0xFF7158E2),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable {
                                viewModel.clearError()
                                onNavigateToSignup()
                            }
                            .bounceClick()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}
