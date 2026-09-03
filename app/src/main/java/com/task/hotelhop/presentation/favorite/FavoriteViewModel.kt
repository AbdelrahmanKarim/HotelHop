package com.task.hotelhop.presentation.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.task.hotelhop.domain.entity.Hotel
import com.task.hotelhop.domain.usecase.hotel.GetFavoriteHotelsUseCase
import com.task.hotelhop.domain.usecase.hotel.ToggleFavoriteUseCase
import com.task.hotelhop.domain.usecase.user.CheckUserLoggedInUseCase
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
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val checkUserLoggedInUseCase: CheckUserLoggedInUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoriteUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<FavoriteUiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            checkUserLoggedInUseCase().collect { isLoggedIn ->
                _uiState.update { it.copy(isLoggedIn = isLoggedIn) }
            }
        }
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
            is FavoriteUiEvent.FavoriteToggled -> requestFavoriteToggle(event.hotel)
            is FavoriteUiEvent.HotelClicked -> viewModelScope.launch {
                _effect.send(FavoriteUiEffect.NavigateToDetails(event.hotelId))
            }
            FavoriteUiEvent.SignInClicked,
            FavoriteUiEvent.LoginRequiredConfirmed -> viewModelScope.launch {
                _uiState.update { it.copy(showLoginRequired = false) }
                _effect.send(FavoriteUiEffect.NavigateToLogin)
            }
            FavoriteUiEvent.LoginRequiredDismissed -> _uiState.update { it.copy(showLoginRequired = false) }
            FavoriteUiEvent.UnfavoriteConfirmed -> confirmUnfavorite()
            FavoriteUiEvent.UnfavoriteDismissed -> _uiState.update { it.copy(pendingUnfavorite = null) }
        }
    }

    private fun requestFavoriteToggle(hotel: Hotel) {
        if (!_uiState.value.isLoggedIn) {
            _uiState.update { it.copy(showLoginRequired = true) }
            return
        }
        if (hotel.isFavorite) {
            _uiState.update { it.copy(pendingUnfavorite = hotel) }
            return
        }
        toggleFavorite(hotel, favorite = true)
    }

    private fun confirmUnfavorite() {
        val hotel = _uiState.value.pendingUnfavorite ?: return
        _uiState.update { it.copy(pendingUnfavorite = null) }
        toggleFavorite(hotel, favorite = false)
    }

    private fun toggleFavorite(hotel: Hotel, favorite: Boolean) {
        viewModelScope.launch {
            runCatching { toggleFavoriteUseCase(hotel.id, favorite) }
                .onFailure { _effect.send(FavoriteUiEffect.ShowSnackbar(it.toUiText())) }
        }
    }
}
