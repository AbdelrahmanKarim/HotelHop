package com.task.hotelhop.presentation.hotel_details

import com.task.hotelhop.domain.entity.Hotel
import com.task.hotelhop.presentation.util.UiText

data class HotelDetailsUiState(
    val isLoading: Boolean = true,
    val hotel: Hotel? = null,
    val isOfflineEmpty: Boolean = false
)

sealed interface HotelDetailsUiEvent {
    data object Retry : HotelDetailsUiEvent
    data object FavoriteToggled : HotelDetailsUiEvent
    data object ViewOnMapClicked : HotelDetailsUiEvent
    data object BookClicked : HotelDetailsUiEvent
    data object BackClicked : HotelDetailsUiEvent
}

sealed interface HotelDetailsUiEffect {
    data object NavigateBack : HotelDetailsUiEffect
    data class NavigateToCheckout(val hotelId: String) : HotelDetailsUiEffect
    data class OpenMap(val locationUrl: String) : HotelDetailsUiEffect
    data class ShowSnackbar(val message: UiText) : HotelDetailsUiEffect
}
