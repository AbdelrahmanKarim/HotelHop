package com.task.hotelhop.presentation.search

import com.task.hotelhop.domain.entity.Hotel
import com.task.hotelhop.presentation.util.UiText

data class SearchUiState(
    val query: String = "",
    val results: List<Hotel> = emptyList(),
    val isSearching: Boolean = false,
    val isEmpty: Boolean = false
)

sealed interface SearchUiEvent {
    data class QueryChanged(val query: String) : SearchUiEvent
    data class FavoriteToggled(val hotel: Hotel) : SearchUiEvent
    data class HotelClicked(val hotelId: String) : SearchUiEvent
}

sealed interface SearchUiEffect {
    data class NavigateToDetails(val hotelId: String) : SearchUiEffect
    data class ShowSnackbar(val message: UiText) : SearchUiEffect
}
