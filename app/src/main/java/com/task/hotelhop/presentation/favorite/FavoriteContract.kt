package com.task.hotelhop.presentation.favorite

import com.task.hotelhop.domain.entity.Hotel
import com.task.hotelhop.presentation.util.UiText

data class FavoriteUiState(
    val isLoading: Boolean = true,
    val hotels: List<Hotel> = emptyList()
)

sealed interface FavoriteUiEvent {
    data class FavoriteToggled(val hotel: Hotel) : FavoriteUiEvent
    data class HotelClicked(val hotelId: String) : FavoriteUiEvent
}

sealed interface FavoriteUiEffect {
    data class NavigateToDetails(val hotelId: String) : FavoriteUiEffect
    data class ShowSnackbar(val message: UiText) : FavoriteUiEffect
}
