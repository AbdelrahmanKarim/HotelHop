package com.task.hotelhop.domain.usecase.hotel

import com.task.hotelhop.domain.repo.HotelRepository

class ToggleFavoriteUseCase(private val repository: HotelRepository) {
    suspend operator fun invoke(hotelId: String, isFavorite: Boolean) {
        repository.toggleFavorite(hotelId, isFavorite)
    }
}