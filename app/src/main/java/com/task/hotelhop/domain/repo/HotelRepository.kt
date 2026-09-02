package com.task.hotelhop.domain.repo


import com.task.hotelhop.domain.entity.Hotel
import kotlinx.coroutines.flow.Flow

interface HotelRepository {
    suspend fun refreshHotels(limit: Int, offset: Int): Int

    fun getHotels(): Flow<List<Hotel>>
    fun getFavoriteHotels(): Flow<List<Hotel>>

    suspend fun toggleFavorite(hotelId: String, isFavorite: Boolean)
    suspend fun getHotelDetails(hotelId: String): Hotel
    suspend fun searchHotels(query: String, limit: Int): List<Hotel>
}