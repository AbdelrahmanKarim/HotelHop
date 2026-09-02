package com.task.hotelhop.data.local.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.task.hotelhop.data.local.entity.HotelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HotelDao {
    @Query("SELECT * FROM hotels")
    fun getAllHotels(): Flow<List<HotelEntity>>

    @Query("SELECT * FROM hotels WHERE isFavorite = 1")
    fun getFavoriteHotels(): Flow<List<HotelEntity>>

    @Query("SELECT * FROM hotels WHERE id = :hotelId")
    suspend fun getHotelById(hotelId: String): HotelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHotels(hotels: List<HotelEntity>)

    @Query("UPDATE hotels SET isFavorite = :isFavorite WHERE id = :hotelId")
    suspend fun updateFavoriteStatus(hotelId: String, isFavorite: Boolean)

    @Query("DELETE FROM hotels WHERE isFavorite = 0")
    suspend fun clearNonFavoriteCache()
}