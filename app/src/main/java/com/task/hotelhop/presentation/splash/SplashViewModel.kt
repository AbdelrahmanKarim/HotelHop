package com.task.hotelhop.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.task.hotelhop.domain.usecase.user.CheckUserLoggedInUseCase
import com.task.hotelhop.domain.usecase.user.ObserveOnboardingUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashViewModel(
    private val observeOnboardingUseCase: ObserveOnboardingUseCase,
    private val checkUserLoggedInUseCase: CheckUserLoggedInUseCase
) : ViewModel() {

    private val _effect = Channel<SplashUiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            delay(700)
            val hasSeenOnboarding = observeOnboardingUseCase().first()
            val isLoggedIn = checkUserLoggedInUseCase().first()
            val destination = when {
                !hasSeenOnboarding -> SplashUiEffect.NavigateToOnboarding
                !isLoggedIn -> SplashUiEffect.NavigateToLogin
                else -> SplashUiEffect.NavigateToHome
            }
            _effect.send(destination)
        }
    }
}
