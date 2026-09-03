package com.task.hotelhop.data.datasource.hotel

import com.task.hotelhop.data.remote.dto.HotelDetailsWrapperDto
import com.task.hotelhop.data.remote.dto.HotelsListResponseDto
import com.task.hotelhop.data.remote.dto.SemanticSearchResponseDto

interface RemoteHotelDataSource {
    suspend fun getHotels(limit: Int, offset: Int): Result<HotelsListResponseDto>
    suspend fun getHotelDetails(hotelId: String): Result<HotelDetailsWrapperDto>
    suspend fun searchHotels(query: String, limit: Int): Result<SemanticSearchResponseDto>
}