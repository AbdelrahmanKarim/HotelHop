package com.task.hotelhop.presentation.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.task.hotelhop.domain.entity.Hotel
import com.task.hotelhop.domain.usecase.hotel.GetFavoriteHotelsUseCase
import com.task.hotelhop.domain.usecase.hotel.ToggleFavoriteUseCase
import com.task.hotelhop.presentation.util.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FavoriteViewModel(
    private val getFavoriteHotelsUseCase: GetFavoriteHotelsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoriteUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<FavoriteUiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            getFavoriteHotelsUseCase()
                .catch { throwable ->
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(FavoriteUiEffect.ShowSnackbar(throwable.toUiText()))
                }
                .collect { hotels ->
                    _uiState.update { it.copy(isLoading = false, hotels = hotels) }
                }
        }
    }

    fun onEvent(event: FavoriteUiEvent) {
        when (event) {
            is FavoriteUiEvent.FavoriteToggled -> toggleFavorite(event.hotel)
            is FavoriteUiEvent.HotelClicked -> viewModelScope.launch {
                _effect.send(FavoriteUiEffect.NavigateToDetails(event.hotelId))
            }
        }
    }

    private fun toggleFavorite(hotel: Hotel) {
        viewModelScope.launch {
            runCatching { toggleFavoriteUseCase(hotel.id, !hotel.isFavorite) }
                .onFailure { _effect.send(FavoriteUiEffect.ShowSnackbar(it.toUiText())) }
        }
    }
}
