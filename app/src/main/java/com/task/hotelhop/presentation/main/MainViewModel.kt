package com.task.hotelhop.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.task.hotelhop.domain.usecase.user.ObserveLanguageUseCase
import com.task.hotelhop.domain.usecase.user.ObserveThemeUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    observeThemeUseCase: ObserveThemeUseCase,
    observeLanguageUseCase: ObserveLanguageUseCase
) : ViewModel() {

    val uiState: StateFlow<MainUiState> = combine(
        observeThemeUseCase(),
        observeLanguageUseCase()
    ) { darkMode, language ->
        MainUiState(
            isDarkTheme = darkMode,
            languageCode = language
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState()
    )
}
