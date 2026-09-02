package com.task.hotelhop.presentation.login

import com.task.hotelhop.presentation.util.UiText

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val emailError: UiText? = null,
    val passwordError: UiText? = null
)

sealed interface LoginUiEvent {
    data class EmailChanged(val value: String) : LoginUiEvent
    data class PasswordChanged(val value: String) : LoginUiEvent
    data object TogglePasswordVisibility : LoginUiEvent
    data object Submit : LoginUiEvent
    data object GoogleSignInClicked : LoginUiEvent
    data class GoogleIdTokenReceived(val idToken: String) : LoginUiEvent
    data class GoogleSignInFailed(val message: UiText) : LoginUiEvent
    data object RegisterClicked : LoginUiEvent
}

sealed interface LoginUiEffect {
    data object NavigateToHome : LoginUiEffect
    data object NavigateToRegister : LoginUiEffect
    data object LaunchGoogleSignIn : LoginUiEffect
    data class ShowSnackbar(val message: UiText) : LoginUiEffect
}
