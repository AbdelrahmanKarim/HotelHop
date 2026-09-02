package com.task.hotelhop.data.datasource.hotel

import com.example.atmos.data.util.safeCall
import com.task.hotelhop.data.remote.dto.HotelDetailsWrapperDto
import com.task.hotelhop.data.remote.dto.HotelsListResponseDto
import com.task.hotelhop.data.remote.dto.SemanticSearchResponseDto
import com.task.hotelhop.data.remote.service.HotelApiService

class RemoteHotelDataSourceImpl(
    private val apiService: HotelApiService
) : RemoteHotelDataSource {

    override suspend fun getHotels(limit: Int, offset: Int): Result<HotelsListResponseDto> {
        return safeCall { apiService.getHotels(limit, offset) }
    }

    override suspend fun getHotelDetails(hotelId: String): Result<HotelDetailsWrapperDto> {
        return safeCall { apiService.getHotelDetails(hotelId) }
    }

    override suspend fun searchHotels(query: String, limit: Int): Result<SemanticSearchResponseDto> {
        return safeCall { apiService.searchHotels(query, limit) }
    }
}