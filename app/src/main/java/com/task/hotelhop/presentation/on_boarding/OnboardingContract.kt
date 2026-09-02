package com.task.hotelhop.presentation.on_boarding

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.ui.graphics.vector.ImageVector
import com.task.hotelhop.R

data class OnboardingUiState(
    val currentPage: Int = 0
)

sealed interface OnboardingUiEvent {
    data class PageChanged(val page: Int) : OnboardingUiEvent
    data object NextClicked : OnboardingUiEvent
    data object GetStartedClicked : OnboardingUiEvent
}

sealed interface OnboardingUiEffect {
    data object NavigateToLogin : OnboardingUiEffect
}

data class OnboardingPage(
    val icon: ImageVector,
    @StringRes val titleRes: Int,
    @StringRes val bodyRes: Int
)

val onboardingPages = listOf(
    OnboardingPage(
        icon = Icons.Outlined.TravelExplore,
        titleRes = R.string.onboarding_search_title,
        bodyRes = R.string.onboarding_search_body
    ),
    OnboardingPage(
        icon = Icons.Outlined.FavoriteBorder,
        titleRes = R.string.onboarding_favorite_title,
        bodyRes = R.string.onboarding_favorite_body
    ),
    OnboardingPage(
        icon = Icons.Outlined.Hotel,
        titleRes = R.string.onboarding_book_title,
        bodyRes = R.string.onboarding_book_body
    )
)
