package com.task.hotelhop.presentation.register

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.task.hotelhop.R
import com.task.hotelhop.domain.usecase.user.SignUpWithEmailUseCase
import com.task.hotelhop.presentation.util.UiText
import com.task.hotelhop.presentation.util.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val signUpWithEmailUseCase: SignUpWithEmailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<RegisterUiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: RegisterUiEvent) {
        when (event) {
            is RegisterUiEvent.FirstNameChanged -> _uiState.update {
                it.copy(firstName = event.value, firstNameError = null)
            }
            is RegisterUiEvent.LastNameChanged -> _uiState.update {
                it.copy(lastName = event.value, lastNameError = null)
            }
            is RegisterUiEvent.EmailChanged -> _uiState.update {
                it.copy(email = event.value, emailError = null)
            }
            is RegisterUiEvent.PasswordChanged -> _uiState.update {
                it.copy(password = event.value, passwordError = null)
            }
            is RegisterUiEvent.GenderChanged -> _uiState.update {
                it.copy(gender = event.value, genderError = null)
            }
            RegisterUiEvent.TogglePasswordVisibility -> _uiState.update {
                it.copy(isPasswordVisible = !it.isPasswordVisible)
            }
            RegisterUiEvent.Submit -> submit()
            RegisterUiEvent.LoginClicked -> viewModelScope.launch {
                _effect.send(RegisterUiEffect.NavigateToLogin)
            }
        }
    }

    private fun submit() {
        val state = _uiState.value
        val firstNameError = state.firstName.trim().takeIf { it.isBlank() }?.let {
            UiText.StringResource(R.string.error_validation_required)
        }
        val lastNameError = state.lastName.trim().takeIf { it.isBlank() }?.let {
            UiText.StringResource(R.string.error_validation_required)
        }
        val emailError = when {
            state.email.isBlank() -> UiText.StringResource(R.string.error_validation_required)
            !Patterns.EMAIL_ADDRESS.matcher(state.email.trim()).matches() ->
                UiText.StringResource(R.string.error_validation_email)
            else -> null
        }
        val passwordError = when {
            state.password.isBlank() -> UiText.StringResource(R.string.error_validation_required)
            state.password.length < 6 -> UiText.StringResource(R.string.error_validation_password)
            else -> null
        }
        val genderError = if (state.gender.isBlank()) {
            UiText.StringResource(R.string.error_validation_gender)
        } else {
            null
        }

        if (listOf(firstNameError, lastNameError, emailError, passwordError, genderError).any { it != null }) {
            _uiState.update {
                it.copy(
                    firstNameError = firstNameError,
                    lastNameError = lastNameError,
                    emailError = emailError,
                    passwordError = passwordError,
                    genderError = genderError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                signUpWithEmailUseCase(
                    fName = state.firstName.trim(),
                    lName = state.lastName.trim(),
                    email = state.email.trim(),
                    pass = state.password,
                    gender = state.gender
                )
            }.onSuccess {
                _effect.send(RegisterUiEffect.NavigateToHome)
            }.onFailure { throwable ->
                _effect.send(RegisterUiEffect.ShowSnackbar(throwable.toUiText()))
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
