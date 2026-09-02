package com.task.hotelhop.data.repo


import com.task.hotelhop.data.datasource.hotel.LocalHotelDataSource
import com.task.hotelhop.data.datasource.hotel.RemoteHotelDataSource
import com.task.hotelhop.data.mapper.toDomain
import com.task.hotelhop.data.mapper.toEntity
import com.task.hotelhop.domain.entity.Hotel
import com.task.hotelhop.domain.exception.AppException
import com.task.hotelhop.domain.repo.HotelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class HotelRepositoryImpl(
    private val remoteDS: RemoteHotelDataSource,
    private val localDS: LocalHotelDataSource
) : HotelRepository {

    override suspend fun refreshHotels(limit: Int, offset: Int) {
        val remoteResult = remoteDS.getHotels(limit, offset)

        remoteResult.onSuccess { dtoResponse ->
            if (offset == 0) localDS.clearNonFavoriteCache()
            val newEntities = dtoResponse.data.map { it.toEntity() }
            val currentCache = localDS.getCachedHotels().first()
            val favoriteIds = currentCache.filter { it.isFavorite }.map { it.id }

            val entitiesToSave = newEntities.map { entity ->
                if (favoriteIds.contains(entity.id)) entity.copy(isFavorite = true) else entity
            }

            localDS.cacheHotels(entitiesToSave)
        }.onFailure {
            val currentCache = localDS.getCachedHotels().first()
            if (currentCache.isEmpty()) {
                throw AppException.OfflineAndNoCacheException()
            } else {
                throw it
            }
        }
    }

    override fun getHotels(): Flow<List<Hotel>> {
        return localDS.getCachedHotels().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getFavoriteHotels(): Flow<List<Hotel>> {
        return localDS.getFavoriteHotels().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun toggleFavorite(hotelId: String, isFavorite: Boolean) {
        localDS.toggleFavorite(hotelId, isFavorite)
    }

    override suspend fun getHotelDetails(hotelId: String): Hotel {
        val remoteResult = remoteDS.getHotelDetails(hotelId)

        return remoteResult.map { it.data.toDomain() }.getOrElse { networkException ->
            val cachedEntity = localDS.getHotelById(hotelId)
            cachedEntity?.toDomain() ?: throw networkException
        }
    }

    override suspend fun searchHotels(
        query: String,
        limit: Int
    ): List<Hotel> {
        val remoteResult = remoteDS.searchHotels(query, limit)
        return remoteResult.getOrThrow().data.map { it.toDomain() }
    }

}