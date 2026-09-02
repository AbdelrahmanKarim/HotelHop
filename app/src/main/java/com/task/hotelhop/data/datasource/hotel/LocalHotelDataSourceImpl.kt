package com.task.hotelhop.data.datasource.hotel

import com.task.hotelhop.data.local.dao.HotelDao
import com.task.hotelhop.data.local.entity.HotelEntity
import kotlinx.coroutines.flow.Flow

class LocalHotelDataSourceImpl(private val dao: HotelDao) : LocalHotelDataSource {
    override fun getCachedHotels(): Flow<List<HotelEntity>> = dao.getAllHotels()
    override fun getFavoriteHotels(): Flow<List<HotelEntity>> = dao.getFavoriteHotels()
    override suspend fun getHotelById(hotelId: String): HotelEntity? = dao.getHotelById(hotelId)
    override suspend fun cacheHotels(hotels: List<HotelEntity>) = dao.insertHotels(hotels)
    override suspend fun toggleFavorite(hotelId: String, isFavorite: Boolean) = dao.updateFavoriteStatus(hotelId, isFavorite)
    override suspend fun clearNonFavoriteCache() = dao.clearNonFavoriteCache()
}