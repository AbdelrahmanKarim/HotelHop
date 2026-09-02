package com.task.hotelhop.presentation.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.task.hotelhop.R
import com.task.hotelhop.domain.usecase.user.LoginWithEmailUseCase
import com.task.hotelhop.domain.usecase.user.SignInWithGoogleUseCase
import com.task.hotelhop.presentation.util.UiText
import com.task.hotelhop.presentation.util.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginWithEmailUseCase: LoginWithEmailUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<LoginUiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: LoginUiEvent) {
        when (event) {
            is LoginUiEvent.EmailChanged -> _uiState.update {
                it.copy(email = event.value, emailError = null)
            }
            is LoginUiEvent.PasswordChanged -> _uiState.update {
                it.copy(password = event.value, passwordError = null)
            }
            LoginUiEvent.TogglePasswordVisibility -> _uiState.update {
                it.copy(isPasswordVisible = !it.isPasswordVisible)
            }
            LoginUiEvent.Submit -> submit()
            LoginUiEvent.GoogleSignInClicked -> viewModelScope.launch {
                _effect.send(LoginUiEffect.LaunchGoogleSignIn)
            }
            is LoginUiEvent.GoogleIdTokenReceived -> signInWithGoogle(event.idToken)
            is LoginUiEvent.GoogleSignInFailed -> viewModelScope.launch {
                _effect.send(LoginUiEffect.ShowSnackbar(event.message))
            }
            LoginUiEvent.RegisterClicked -> viewModelScope.launch {
                _effect.send(LoginUiEffect.NavigateToRegister)
            }
        }
    }

    private fun submit() {
        val state = _uiState.value
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
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                loginWithEmailUseCase(state.email.trim(), state.password)
            }.onSuccess {
                _effect.send(LoginUiEffect.NavigateToHome)
            }.onFailure { throwable ->
                _effect.send(LoginUiEffect.ShowSnackbar(throwable.toUiText()))
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { signInWithGoogleUseCase(idToken) }
                .onSuccess { _effect.send(LoginUiEffect.NavigateToHome) }
                .onFailure { _effect.send(LoginUiEffect.ShowSnackbar(it.toUiText())) }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
