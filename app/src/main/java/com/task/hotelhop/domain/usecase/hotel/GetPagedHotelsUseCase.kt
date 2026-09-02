package com.task.hotelhop.domain.usecase.hotel

import com.task.hotelhop.domain.entity.Hotel
import com.task.hotelhop.domain.repo.HotelRepository
import kotlinx.coroutines.flow.Flow

class GetPagedHotelsUseCase(private val repository: HotelRepository) {
    suspend operator fun invoke(limit: Int, offset: Int) {
        repository.refreshHotels(limit, offset)
    }

    // Optional helper to observe the local database stream
    fun observeHotels(): Flow<List<Hotel>> = repository.getHotels()
}