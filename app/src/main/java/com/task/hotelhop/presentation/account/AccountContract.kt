package com.task.hotelhop.presentation.account

import com.task.hotelhop.domain.entity.User
import com.task.hotelhop.presentation.util.UiText

enum class ThemeMode { LIGHT, DARK, SYSTEM }

data class AccountUiState(
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean = false,
    val user: User? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val languageCode: String = "en",
    val showLogoutConfirm: Boolean = false
)

sealed interface AccountUiEvent {
    data class ThemeSelected(val mode: ThemeMode) : AccountUiEvent
    data class LanguageSelected(val languageCode: String) : AccountUiEvent
    data object SignInClicked : AccountUiEvent
    data object LogoutClicked : AccountUiEvent
    data object LogoutConfirmed : AccountUiEvent
    data object LogoutDismissed : AccountUiEvent
}

sealed interface AccountUiEffect {
    data object NavigateToLogin : AccountUiEffect
    data object LoggedOut : AccountUiEffect
    data class RecreateForLanguage(val languageCode: String) : AccountUiEffect
    data class ShowSnackbar(val message: UiText) : AccountUiEffect
}
