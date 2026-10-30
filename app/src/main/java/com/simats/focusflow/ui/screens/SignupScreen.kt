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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.ui.components.CustomTopBar
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.theme.DarkBackground
import com.simats.focusflow.ui.theme.DarkBorder
import com.simats.focusflow.ui.theme.DarkPrimary
import com.simats.focusflow.ui.theme.DarkPrimaryLight
import com.simats.focusflow.ui.theme.DarkSurface
import com.simats.focusflow.ui.theme.DarkSurfaceVariant
import com.simats.focusflow.ui.theme.DarkTextMuted
import com.simats.focusflow.ui.theme.DarkTextPrimary
import com.simats.focusflow.ui.theme.SuccessGreen
import com.simats.focusflow.ui.viewmodel.AuthUiState
import com.simats.focusflow.ui.viewmodel.AuthViewModel

/**
 * Modern minimalist Signup Screen for FocusFlow.
 * Styled in dark canvas (#0F0F14), elevated surface card (#181722), and violet accents (#8B5CF6).
 * Connected to flow_backend via MVVM architecture and StateFlow.
 */
@Composable
fun SignupScreen(
    onBack: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onLogin: () -> Unit = onNavigateToLogin,
    onSignupSuccess: (userId: Int) -> Unit = {},
    onContinue: () -> Unit = {},
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.initSessionManager(SessionManager.getInstance(context))
    }

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

    val isNameValid = name.isBlank() || name.trim().length >= 2
    val isEmailValid = email.isBlank() || (email.contains("@") && email.contains(".") && email.length >= 5)
    val isPasswordValid = password.isBlank() || password.length >= 6
    val passwordsMatch = confirmPassword.isBlank() || (password == confirmPassword)

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            val userId = uiState.user?.id ?: 1
            onSignupSuccess(userId)
            onContinue()
            viewModel.resetState()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { errorMsg ->
            snackbarHostState.showSnackbar(errorMsg)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(DarkBackground)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            CustomTopBar(
                title = "Create Account",
                onBack = onBack,
                titleColor = DarkTextPrimary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App Brand Logo & Name
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "FocusFlow Logo",
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "FocusFlow",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkTextPrimary
                    )
                    Text(
                        text = "Deep work and mindful habits",
                        fontSize = 12.sp,
                        color = DarkTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Join mindful achievers building focused work habits.",
                fontSize = 14.sp,
                color = DarkTextMuted,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Error Banner Display
            if (uiState.errorMessage != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    color = Color(0xFF2A1519),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.padding(end = 10.dp).size(18.dp)
                        )
                        Text(
                            text = uiState.errorMessage ?: "",
                            fontSize = 13.sp,
                            color = Color(0xFFFCA5A5),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = Color(0xFFFCA5A5),
                            modifier = Modifier
                                .clickable { viewModel.clearError() }
                                .padding(start = 8.dp)
                                .size(16.dp)
                        )
                    }
                }
            }

            // Elevated Form Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Name Field
                    Text(
                        text = "Full Name",
                        fontSize = 13.sp,
                        color = DarkTextMuted,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (uiState.errorMessage != null) viewModel.clearError()
                        },
                        placeholder = { Text("e.g. Sri Sai", color = DarkTextMuted, fontSize = 14.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp),
                        shape = RoundedCornerShape(12.dp),
                        isError = (hasAttemptedSubmit && name.isBlank()) || !isNameValid,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = DarkTextPrimary,
                            unfocusedTextColor = DarkTextPrimary,
                            focusedContainerColor = DarkBackground,
                            unfocusedContainerColor = DarkBackground,
                            errorBorderColor = Color(0xFFEF4444)
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        trailingIcon = {
                            if (name.isNotBlank() && isNameValid) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Valid name",
                                    tint = SuccessGreen,
                                    modifier = Modifier.padding(end = 12.dp).size(18.dp)
                                )
                            }
                        }
                    )
                    if (hasAttemptedSubmit && name.isBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Full name is required",
                            fontSize = 12.sp,
                            color = Color(0xFFEF4444)
                        )
                    } else if (!isNameValid) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Name must be at least 2 characters long",
                            fontSize = 12.sp,
                            color = Color(0xFFEF4444)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email Field
                    Text(
                        text = "Email Address",
                        fontSize = 13.sp,
                        color = DarkTextMuted,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (uiState.errorMessage != null) viewModel.clearError()
                        },
                        placeholder = { Text("you@example.com", color = DarkTextMuted, fontSize = 14.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp),
                        shape = RoundedCornerShape(12.dp),
                        isError = (hasAttemptedSubmit && email.isBlank()) || !isEmailValid,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = DarkTextPrimary,
                            unfocusedTextColor = DarkTextPrimary,
                            focusedContainerColor = DarkBackground,
                            unfocusedContainerColor = DarkBackground,
                            errorBorderColor = Color(0xFFEF4444)
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        trailingIcon = {
                            if (email.isNotBlank() && isEmailValid) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Valid email",
                                    tint = SuccessGreen,
                                    modifier = Modifier.padding(end = 12.dp).size(18.dp)
                                )
                            }
                        }
                    )
                    if (hasAttemptedSubmit && email.isBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Email address is required",
                            fontSize = 12.sp,
                            color = Color(0xFFEF4444)
                        )
                    } else if (!isEmailValid) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Please enter a valid email address",
                            fontSize = 12.sp,
                            color = Color(0xFFEF4444)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    Text(
                        text = "Password",
                        fontSize = 13.sp,
                        color = DarkTextMuted,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (uiState.errorMessage != null) viewModel.clearError()
                        },
                        placeholder = { Text("At least 6 characters", color = DarkTextMuted, fontSize = 14.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp),
                        shape = RoundedCornerShape(12.dp),
                        isError = (!isPasswordValid) || (hasAttemptedSubmit && password.isBlank()),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = DarkTextPrimary,
                            unfocusedTextColor = DarkTextPrimary,
                            focusedContainerColor = DarkBackground,
                            unfocusedContainerColor = DarkBackground,
                            errorBorderColor = Color(0xFFEF4444)
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
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.padding(end = 8.dp).size(16.dp)
                                    )
                                }
                                Text(
                                    text = if (passwordVisible) "Hide" else "Show",
                                    color = DarkPrimaryLight,
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
                            color = Color(0xFFEF4444)
                        )
                    } else if (!isPasswordValid) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Password must be at least 6 characters",
                            fontSize = 12.sp,
                            color = Color(0xFFEF4444)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Confirm Password Field
                    Text(
                        text = "Confirm Password",
                        fontSize = 13.sp,
                        color = DarkTextMuted,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            if (uiState.errorMessage != null) viewModel.clearError()
                        },
                        placeholder = { Text("Re-enter your password", color = DarkTextMuted, fontSize = 14.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp),
                        shape = RoundedCornerShape(12.dp),
                        isError = (!passwordsMatch) || (hasAttemptedSubmit && confirmPassword.isBlank()),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = DarkTextPrimary,
                            unfocusedTextColor = DarkTextPrimary,
                            focusedContainerColor = DarkBackground,
                            unfocusedContainerColor = DarkBackground,
                            errorBorderColor = Color(0xFFEF4444)
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
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.padding(end = 8.dp).size(16.dp)
                                    )
                                }
                                Text(
                                    text = if (confirmPasswordVisible) "Hide" else "Show",
                                    color = DarkPrimaryLight,
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
                            color = Color(0xFFEF4444)
                        )
                    } else if (!passwordsMatch) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Passwords do not match",
                            fontSize = 12.sp,
                            color = Color(0xFFEF4444)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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
                                checkedColor = DarkPrimary,
                                uncheckedColor = DarkTextMuted,
                                checkmarkColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "I agree to the Terms of Service & Privacy Policy",
                            fontSize = 13.sp,
                            color = DarkTextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Signup Action Button (Solid violet #8B5CF6 with rounded corners)
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
                            containerColor = DarkPrimary,
                            disabledContainerColor = DarkPrimary.copy(alpha = 0.5f)
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
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

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
                        color = DarkTextMuted,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Log In",
                        color = DarkPrimaryLight,
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
        onSignupSuccess = { onContinue() },
        onNavigateToLogin = onLogin
    )
}

@Preview(showBackground = true)
@Composable
fun SignupScreenPreview() {
    SignupScreen()
}
