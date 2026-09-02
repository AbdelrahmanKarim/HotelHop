package com.task.hotelhop.presentation.hotel_details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.task.hotelhop.R
import com.task.hotelhop.domain.exception.AppException
import com.task.hotelhop.domain.usecase.hotel.GetFavoriteHotelsUseCase
import com.task.hotelhop.domain.usecase.hotel.GetHotelDetailsUseCase
import com.task.hotelhop.domain.usecase.hotel.ToggleFavoriteUseCase
import com.task.hotelhop.presentation.navigation.Screen
import com.task.hotelhop.presentation.util.UiText
import com.task.hotelhop.presentation.util.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HotelDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getHotelDetailsUseCase: GetHotelDetailsUseCase,
    private val getFavoriteHotelsUseCase: GetFavoriteHotelsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val hotelId: String = checkNotNull(savedStateHandle[Screen.HotelDetails.ARG_HOTEL_ID])

    private val _uiState = MutableStateFlow(HotelDetailsUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<HotelDetailsUiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadDetails()
        observeFavoriteStatus()
    }

    private fun observeFavoriteStatus() {
        viewModelScope.launch {
            getFavoriteHotelsUseCase().collect { favorites ->
                val isFavorite = favorites.any { it.id == hotelId }
                _uiState.update { state ->
                    state.copy(hotel = state.hotel?.copy(isFavorite = isFavorite))
                }
            }
        }
    }

    fun onEvent(event: HotelDetailsUiEvent) {
        when (event) {
            HotelDetailsUiEvent.Retry -> loadDetails()
            HotelDetailsUiEvent.FavoriteToggled -> toggleFavorite()
            HotelDetailsUiEvent.ViewOnMapClicked -> openMap()
            HotelDetailsUiEvent.BookClicked -> viewModelScope.launch {
                _effect.send(HotelDetailsUiEffect.NavigateToCheckout(hotelId))
            }
            HotelDetailsUiEvent.BackClicked -> viewModelScope.launch {
                _effect.send(HotelDetailsUiEffect.NavigateBack)
            }
        }
    }

    private fun loadDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isOfflineEmpty = false) }
            runCatching { getHotelDetailsUseCase(hotelId) }
                .onSuccess { hotel ->
                    _uiState.update { it.copy(isLoading = false, hotel = hotel, isOfflineEmpty = false) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isOfflineEmpty = throwable is AppException.NetworkException ||
                                throwable is AppException.OfflineAndNoCacheException
                        )
                    }
                    _effect.send(HotelDetailsUiEffect.ShowSnackbar(throwable.toUiText()))
                }
        }
    }

    private fun toggleFavorite() {
        val hotel = _uiState.value.hotel ?: return
        viewModelScope.launch {
            runCatching { toggleFavoriteUseCase(hotel.id, !hotel.isFavorite) }
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(hotel = state.hotel?.copy(isFavorite = !hotel.isFavorite))
                    }
                }
                .onFailure { _effect.send(HotelDetailsUiEffect.ShowSnackbar(it.toUiText())) }
        }
    }

    private fun openMap() {
        viewModelScope.launch {
            val url = _uiState.value.hotel?.locationUrl.orEmpty()
            if (url.isBlank()) {
                _effect.send(HotelDetailsUiEffect.ShowSnackbar(UiText.StringResource(R.string.error_map_unavailable)))
            } else {
                _effect.send(HotelDetailsUiEffect.OpenMap(url))
            }
        }
    }
}
