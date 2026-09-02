package com.task.hotelhop.presentation.on_boarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.task.hotelhop.domain.usecase.user.CompleteOnboardingUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val completeOnboardingUseCase: CompleteOnboardingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<OnboardingUiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: OnboardingUiEvent) {
        when (event) {
            is OnboardingUiEvent.PageChanged -> {
                _uiState.update { it.copy(currentPage = event.page) }
            }
            OnboardingUiEvent.NextClicked -> {
                val next = (_uiState.value.currentPage + 1).coerceAtMost(onboardingPages.lastIndex)
                _uiState.update { it.copy(currentPage = next) }
            }
            OnboardingUiEvent.GetStartedClicked -> completeOnboarding()
        }
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            runCatching { completeOnboardingUseCase() }
            _effect.send(OnboardingUiEffect.NavigateToLogin)
        }
    }
}
