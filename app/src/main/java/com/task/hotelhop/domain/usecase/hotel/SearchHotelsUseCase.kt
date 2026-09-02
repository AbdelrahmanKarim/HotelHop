package com.task.hotelhop.domain.usecase.hotel

import com.task.hotelhop.domain.entity.Hotel
import com.task.hotelhop.domain.repo.HotelRepository

class SearchHotelsUseCase(private val repository: HotelRepository) {
    suspend operator fun invoke(query: String, limit: Int = 10): List<Hotel> {
        return repository.searchHotels(query, limit)
    }
}