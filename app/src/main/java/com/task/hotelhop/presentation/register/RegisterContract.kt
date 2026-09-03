package com.task.hotelhop.presentation.register

import com.task.hotelhop.presentation.util.UiText

data class RegisterUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val gender: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val firstNameError: UiText? = null,
    val lastNameError: UiText? = null,
    val emailError: UiText? = null,
    val passwordError: UiText? = null,
    val genderError: UiText? = null
)

sealed interface RegisterUiEvent {
    data class FirstNameChanged(val value: String) : RegisterUiEvent
    data class LastNameChanged(val value: String) : RegisterUiEvent
    data class EmailChanged(val value: String) : RegisterUiEvent
    data class PasswordChanged(val value: String) : RegisterUiEvent
    data class GenderChanged(val value: String) : RegisterUiEvent
    data object TogglePasswordVisibility : RegisterUiEvent
    data object Submit : RegisterUiEvent
    data object LoginClicked : RegisterUiEvent
}

sealed interface RegisterUiEffect {
    data object NavigateToHome : RegisterUiEffect
    data object NavigateToLogin : RegisterUiEffect
    data class ShowSnackbar(val message: UiText) : RegisterUiEffect
}

object GenderOptions {
    const val MALE = "Male"
    const val FEMALE = "Female"
    const val UNSPECIFIED = "Prefer not to say"
}
