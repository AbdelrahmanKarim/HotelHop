package com.task.hotelhop.domain.usecase.hotel

import com.task.hotelhop.domain.entity.Hotel
import com.task.hotelhop.domain.repo.HotelRepository
import kotlinx.coroutines.flow.Flow

class GetFavoriteHotelsUseCase(private val repository: HotelRepository) {
    operator fun invoke(): Flow<List<Hotel>> {
        return repository.getFavoriteHotels()
    }
}