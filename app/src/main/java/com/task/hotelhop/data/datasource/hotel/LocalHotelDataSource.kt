package com.task.hotelhop.data.datasource.hotel

import com.task.hotelhop.data.local.entity.HotelEntity
import kotlinx.coroutines.flow.Flow

interface LocalHotelDataSource {
    fun getCachedHotels(): Flow<List<HotelEntity>>
    fun getFavoriteHotels(): Flow<List<HotelEntity>>
    suspend fun getHotelById(hotelId: String): HotelEntity?
    suspend fun cacheHotels(hotels: List<HotelEntity>)
    suspend fun replaceNonFavoriteCache(hotels: List<HotelEntity>)
    suspend fun toggleFavorite(hotelId: String, isFavorite: Boolean)
}