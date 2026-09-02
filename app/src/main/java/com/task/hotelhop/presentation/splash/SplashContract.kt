package com.task.hotelhop.presentation.splash

sealed interface SplashUiEffect {
    data object NavigateToOnboarding : SplashUiEffect
    data object NavigateToLogin : SplashUiEffect
    data object NavigateToHome : SplashUiEffect
}
