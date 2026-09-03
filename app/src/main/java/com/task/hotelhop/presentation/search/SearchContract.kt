package com.task.hotelhop.presentation.search

import com.task.hotelhop.domain.entity.Hotel
import com.task.hotelhop.presentation.util.UiText

enum class PriceFilter { ANY, BUDGET, MID, PREMIUM }

enum class RatingFilter { ANY, THREE_PLUS, FOUR_PLUS, FOUR_FIVE_PLUS }

data class SearchUiState(
    val query: String = "",
    val results: List<Hotel> = emptyList(),
    val isSearching: Boolean = false,
    val isEmpty: Boolean = false,
    val isLoggedIn: Boolean = false,
    val priceFilter: PriceFilter = PriceFilter.ANY,
    val ratingFilter: RatingFilter = RatingFilter.ANY,
    val showLoginRequired: Boolean = false,
    val pendingUnfavorite: Hotel? = null
) {
    val hasActiveFilters: Boolean
        get() = priceFilter != PriceFilter.ANY || ratingFilter != RatingFilter.ANY

    val isIdle: Boolean
        get() = query.isBlank() && !hasActiveFilters && results.isEmpty() && !isSearching
}

sealed interface SearchUiEvent {
    data class QueryChanged(val query: String) : SearchUiEvent
    data class PriceFilterSelected(val filter: PriceFilter) : SearchUiEvent
    data class RatingFilterSelected(val filter: RatingFilter) : SearchUiEvent
    data class FavoriteToggled(val hotel: Hotel) : SearchUiEvent
    data class HotelClicked(val hotelId: String) : SearchUiEvent
    data object LoginRequiredConfirmed : SearchUiEvent
    data object LoginRequiredDismissed : SearchUiEvent
    data object UnfavoriteConfirmed : SearchUiEvent
    data object UnfavoriteDismissed : SearchUiEvent
}

sealed interface SearchUiEffect {
    data class NavigateToDetails(val hotelId: String) : SearchUiEffect
    data object NavigateToLogin : SearchUiEffect
    data class ShowSnackbar(val message: UiText) : SearchUiEffect
}
