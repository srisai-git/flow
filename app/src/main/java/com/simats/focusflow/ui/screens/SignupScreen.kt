package com.simats.focusflow.ui.screens

import android.os.Build
import android.view.View
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalView
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
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.viewmodel.AuthViewModel
import com.simats.focusflow.utils.AuthValidator

@Composable
fun SignupScreen(
    onBack: () -> Unit = {},
    onContinue: () -> Unit = {},
    onSignupSuccess: (userId: Int) -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager.getInstance(context) }

    val view = LocalView.current
    DisposableEffect(view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            view.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
        }
        onDispose {}
    }

    LaunchedEffect(Unit) {
        viewModel.initSessionManager(context, sessionManager)
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
    val isEmailValid = email.isBlank() || AuthValidator.isValidEmail(email)
    val isPasswordValid = password.isBlank() || (password.length >= 6 && com.simats.focusflow.utils.SecurityPasswordHelper.isSecurePassword(password))
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
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .padding(horizontal = 20.dp)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            CustomTopBar(
                title = "Create Account",
                onBack = onBack,
                titleColor = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9))
                        .border(1.dp, Color(0xFFE2E8F0), CircleShape),
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
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Start your intentional routine today",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Join thousands of builders cultivating authentic focus daily.",
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (uiState.errorMessage != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    color = Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f))
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
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.padding(end = 10.dp).size(18.dp)
                        )
                        Text(
                            text = uiState.errorMessage ?: "",
                            fontSize = 13.sp,
                            color = Color(0xFFB91C1C),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier
                                .clickable { viewModel.clearError() }
                                .padding(start = 8.dp)
                                .size(16.dp)
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Your Full Name",
                        fontSize = 13.sp,
                        color = Color(0xFF475569),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (uiState.errorMessage != null) viewModel.clearError()
                        },
                        placeholder = { Text("e.g. Alex Morgan", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp),
                        shape = RoundedCornerShape(12.dp),
                        isError = hasAttemptedSubmit && (name.isBlank() || !isNameValid),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            errorTextColor = Color(0xFF0F172A),
                            cursorColor = Color(0xFF7C3AED),
                            errorCursorColor = Color(0xFFDC2626),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            errorContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF7C3AED),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            errorBorderColor = Color(0xFFDC2626)
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )
                    if (hasAttemptedSubmit && name.isBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Name is required",
                            fontSize = 12.sp,
                            color = Color(0xFFDC2626)
                        )
                    } else if (hasAttemptedSubmit && !isNameValid) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Name must be at least 2 characters",
                            fontSize = 12.sp,
                            color = Color(0xFFDC2626)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Email Address",
                        fontSize = 13.sp,
                        color = Color(0xFF475569),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (uiState.errorMessage != null) viewModel.clearError()
                        },
                        placeholder = { Text("you@example.com", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp),
                        shape = RoundedCornerShape(12.dp),
                        isError = hasAttemptedSubmit && (email.isBlank() || !isEmailValid),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            errorTextColor = Color(0xFF0F172A),
                            cursorColor = Color(0xFF7C3AED),
                            errorCursorColor = Color(0xFFDC2626),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            errorContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF7C3AED),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            errorBorderColor = Color(0xFFDC2626)
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
                                    tint = Color(0xFF10B981),
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
                            color = Color(0xFFDC2626)
                        )
                    } else if (hasAttemptedSubmit && !isEmailValid) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = AuthValidator.INVALID_EMAIL_MESSAGE,
                            fontSize = 12.sp,
                            color = Color(0xFFDC2626)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Password",
                        fontSize = 13.sp,
                        color = Color(0xFF475569),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (uiState.errorMessage != null) viewModel.clearError()
                        },
                        placeholder = { Text("At least 6 characters", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp),
                        shape = RoundedCornerShape(12.dp),
                        isError = hasAttemptedSubmit && (password.isBlank() || !isPasswordValid),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            errorTextColor = Color(0xFF0F172A),
                            cursorColor = Color(0xFF7C3AED),
                            errorCursorColor = Color(0xFFDC2626),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            errorContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF7C3AED),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            errorBorderColor = Color(0xFFDC2626)
                        ),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next,
                            autoCorrectEnabled = false
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
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.padding(end = 8.dp).size(16.dp)
                                    )
                                }
                                Text(
                                    text = if (passwordVisible) "Hide" else "Show",
                                    color = Color(0xFF7C3AED),
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
                    } else if (hasAttemptedSubmit && !isPasswordValid) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Password must be at least 6 characters",
                            fontSize = 12.sp,
                            color = Color(0xFFDC2626)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Confirm Password",
                        fontSize = 13.sp,
                        color = Color(0xFF475569),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            if (uiState.errorMessage != null) viewModel.clearError()
                        },
                        placeholder = { Text("Re-enter your password", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp),
                        shape = RoundedCornerShape(12.dp),
                        isError = hasAttemptedSubmit && (confirmPassword.isBlank() || !passwordsMatch),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            errorTextColor = Color(0xFF0F172A),
                            cursorColor = Color(0xFF7C3AED),
                            errorCursorColor = Color(0xFFDC2626),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            errorContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF7C3AED),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            errorBorderColor = Color(0xFFDC2626)
                        ),
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                            autoCorrectEnabled = false
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                hasAttemptedSubmit = true
                                if (agreedToTerms) {
                                    if (name.isBlank() || name.trim().length < 2 ||
                                        email.isBlank() || !AuthValidator.isValidEmail(email) ||
                                        password.isBlank() || password.length < 6 ||
                                        confirmPassword.isBlank() || password != confirmPassword
                                    ) {
                                        return@KeyboardActions
                                    }
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
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.padding(end = 8.dp).size(16.dp)
                                    )
                                }
                                Text(
                                    text = if (confirmPasswordVisible) "Hide" else "Show",
                                    color = Color(0xFF7C3AED),
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
                    } else if (hasAttemptedSubmit && !passwordsMatch) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Passwords do not match",
                            fontSize = 12.sp,
                            color = Color(0xFFDC2626)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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
                                checkedColor = Color(0xFF7C3AED),
                                uncheckedColor = Color(0xFF94A3B8),
                                checkmarkColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "I agree to the Terms of Service & Privacy Policy",
                            fontSize = 13.sp,
                            color = Color(0xFF475569)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            hasAttemptedSubmit = true
                            if (!agreedToTerms) {
                                viewModel.clearError()
                                return@Button
                            }
                            if (name.isBlank() || name.trim().length < 2 ||
                                email.isBlank() || !AuthValidator.isValidEmail(email) ||
                                password.isBlank() || password.length < 6 ||
                                confirmPassword.isBlank() || password != confirmPassword
                            ) {
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
                            .height(54.dp)
                            .bounceClick(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7C3AED),
                            disabledContainerColor = Color(0xFF7C3AED).copy(alpha = 0.5f)
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
                        color = Color(0xFF64748B),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Log In",
                        color = Color(0xFF7C3AED),
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
