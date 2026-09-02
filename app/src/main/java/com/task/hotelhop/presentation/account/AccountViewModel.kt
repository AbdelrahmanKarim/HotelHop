package com.task.hotelhop.presentation.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.task.hotelhop.domain.usecase.user.ChangeLanguageUseCase
import com.task.hotelhop.domain.usecase.user.GetUserDetailsUseCase
import com.task.hotelhop.domain.usecase.user.LogOutUseCase
import com.task.hotelhop.domain.usecase.user.ObserveLanguageUseCase
import com.task.hotelhop.domain.usecase.user.ObserveThemeUseCase
import com.task.hotelhop.domain.usecase.user.ToggleThemeUseCase
import com.task.hotelhop.presentation.util.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccountViewModel(
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val observeThemeUseCase: ObserveThemeUseCase,
    private val observeLanguageUseCase: ObserveLanguageUseCase,
    private val toggleThemeUseCase: ToggleThemeUseCase,
    private val changeLanguageUseCase: ChangeLanguageUseCase,
    private val logOutUseCase: LogOutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<AccountUiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            val user = runCatching { getUserDetailsUseCase() }.getOrNull()
            _uiState.update { it.copy(user = user, isLoading = false) }
        }
        viewModelScope.launch {
            combine(
                observeThemeUseCase(),
                observeLanguageUseCase()
            ) { darkMode, language ->
                darkMode to language
            }.collect { (darkMode, language) ->
                _uiState.update {
                    it.copy(
                        themeMode = when (darkMode) {
                            true -> ThemeMode.DARK
                            false -> ThemeMode.LIGHT
                            null -> ThemeMode.SYSTEM
                        },
                        languageCode = language
                    )
                }
            }
        }
    }

    fun onEvent(event: AccountUiEvent) {
        when (event) {
            is AccountUiEvent.ThemeSelected -> setTheme(event.mode)
            is AccountUiEvent.LanguageSelected -> setLanguage(event.languageCode)
            AccountUiEvent.LogoutClicked -> _uiState.update { it.copy(showLogoutConfirm = true) }
            AccountUiEvent.LogoutDismissed -> _uiState.update { it.copy(showLogoutConfirm = false) }
            AccountUiEvent.LogoutConfirmed -> logout()
        }
    }

    private fun setTheme(mode: ThemeMode) {
        viewModelScope.launch {
            val isDark = when (mode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> null
            }
            runCatching { toggleThemeUseCase(isDark) }
                .onFailure { _effect.send(AccountUiEffect.ShowSnackbar(it.toUiText())) }
        }
    }

    private fun setLanguage(languageCode: String) {
        if (languageCode == _uiState.value.languageCode) return
        viewModelScope.launch {
            runCatching { changeLanguageUseCase(languageCode) }
                .onSuccess { _effect.send(AccountUiEffect.RecreateForLanguage(languageCode)) }
                .onFailure { _effect.send(AccountUiEffect.ShowSnackbar(it.toUiText())) }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(showLogoutConfirm = false) }
            runCatching { logOutUseCase() }
                .onSuccess { _effect.send(AccountUiEffect.NavigateToLogin) }
                .onFailure { _effect.send(AccountUiEffect.ShowSnackbar(it.toUiText())) }
        }
    }
}
