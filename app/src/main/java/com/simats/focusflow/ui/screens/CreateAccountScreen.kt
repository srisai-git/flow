package com.simats.focusflow.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.focusflow.ui.viewmodel.AuthViewModel

/**
 * CreateAccountScreen implementation delegating to the production-ready SignupScreen.
 */
@Composable
fun CreateAccountScreen(
    onBack: () -> Unit = {},
    onContinue: () -> Unit = {},
    onLogin: () -> Unit = {},
    viewModel: AuthViewModel = viewModel()
) {
    SignupScreen(
        onBack = onBack,
        onNavigateToLogin = onLogin,
        onSignupSuccess = onContinue,
        viewModel = viewModel
    )
}

@Preview(showBackground = true)
@Composable
fun CreateAccountScreenPreview() {
    CreateAccountScreen()
}
