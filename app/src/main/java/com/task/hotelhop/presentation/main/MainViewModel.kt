package com.task.hotelhop.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.task.hotelhop.domain.usecase.user.CheckUserLoggedInUseCase
import com.task.hotelhop.domain.usecase.user.ObserveGuestUseCase
import com.task.hotelhop.domain.usecase.user.ObserveLanguageUseCase
import com.task.hotelhop.domain.usecase.user.ObserveOnboardingUseCase
import com.task.hotelhop.domain.usecase.user.ObserveThemeUseCase
import com.task.hotelhop.presentation.navigation.Screen
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    observeThemeUseCase: ObserveThemeUseCase,
    observeLanguageUseCase: ObserveLanguageUseCase,
    observeOnboardingUseCase: ObserveOnboardingUseCase,
    checkUserLoggedInUseCase: CheckUserLoggedInUseCase,
    observeGuestUseCase: ObserveGuestUseCase
) : ViewModel() {

    private val startDestination = flow {
        val hasSeenOnboarding = observeOnboardingUseCase().first()
        val isLoggedIn = checkUserLoggedInUseCase().first()
        val isGuest = observeGuestUseCase().first()
        emit(
            when {
                !hasSeenOnboarding -> Screen.Onboarding.route
                isLoggedIn || isGuest -> Screen.Home.route
                else -> Screen.Login.route
            }
        )
    }

    val uiState = combine(
        observeThemeUseCase(),
        observeLanguageUseCase(),
        startDestination
    ) { darkMode, language, destination ->
        MainUiState(
            isDarkTheme = darkMode,
            languageCode = language,
            startDestination = destination
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = MainUiState()
    )
}
