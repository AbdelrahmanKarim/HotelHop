package com.task.hotelhop.domain.usecase.hotel

import com.task.hotelhop.domain.entity.Hotel
import com.task.hotelhop.domain.repo.HotelRepository

class GetHotelDetailsUseCase(private val repository: HotelRepository) {
    suspend operator fun invoke(hotelId: String): Hotel {
        return repository.getHotelDetails(hotelId)
    }
}