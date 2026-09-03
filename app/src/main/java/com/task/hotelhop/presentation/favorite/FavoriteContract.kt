package com.task.hotelhop.presentation.favorite

import com.task.hotelhop.domain.entity.Hotel
import com.task.hotelhop.presentation.util.UiText

data class FavoriteUiState(
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean = false,
    val hotels: List<Hotel> = emptyList(),
    val showLoginRequired: Boolean = false,
    val pendingUnfavorite: Hotel? = null
)

sealed interface FavoriteUiEvent {
    data class FavoriteToggled(val hotel: Hotel) : FavoriteUiEvent
    data class HotelClicked(val hotelId: String) : FavoriteUiEvent
    data object SignInClicked : FavoriteUiEvent
    data object LoginRequiredConfirmed : FavoriteUiEvent
    data object LoginRequiredDismissed : FavoriteUiEvent
    data object UnfavoriteConfirmed : FavoriteUiEvent
    data object UnfavoriteDismissed : FavoriteUiEvent
}

sealed interface FavoriteUiEffect {
    data class NavigateToDetails(val hotelId: String) : FavoriteUiEffect
    data object NavigateToLogin : FavoriteUiEffect
    data class ShowSnackbar(val message: UiText) : FavoriteUiEffect
}
