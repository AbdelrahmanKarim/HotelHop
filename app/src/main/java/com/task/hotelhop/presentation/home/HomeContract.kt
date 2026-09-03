package com.task.hotelhop.presentation.home

import com.task.hotelhop.domain.entity.Hotel
import com.task.hotelhop.presentation.util.UiText

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isLoggedIn: Boolean = false,
    val hotels: List<Hotel> = emptyList(),
    val popularHotels: List<Hotel> = emptyList(),
    val bestPriceHotels: List<Hotel> = emptyList(),
    val isOfflineEmpty: Boolean = false,
    val endReached: Boolean = false,
    val showLoginRequired: Boolean = false,
    val pendingUnfavorite: Hotel? = null
)

sealed interface HomeUiEvent {
    data object Refresh : HomeUiEvent
    data object Retry : HomeUiEvent
    data object LoadNextPage : HomeUiEvent
    data class FavoriteToggled(val hotel: Hotel) : HomeUiEvent
    data class HotelClicked(val hotelId: String) : HomeUiEvent
    data object PromoClicked : HomeUiEvent
    data object LoginRequiredConfirmed : HomeUiEvent
    data object LoginRequiredDismissed : HomeUiEvent
    data object UnfavoriteConfirmed : HomeUiEvent
    data object UnfavoriteDismissed : HomeUiEvent
}

sealed interface HomeUiEffect {
    data class NavigateToDetails(val hotelId: String) : HomeUiEffect
    data object NavigateToLogin : HomeUiEffect
    data class ShowSnackbar(val message: UiText) : HomeUiEffect
}
