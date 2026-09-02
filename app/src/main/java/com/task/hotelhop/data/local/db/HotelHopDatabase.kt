package com.task.hotelhop.data.local.db


import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.task.hotelhop.data.local.converters.StringListConverter
import com.task.hotelhop.data.local.dao.HotelDao
import com.task.hotelhop.data.local.entity.HotelEntity

@Database(
    entities = [HotelEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(StringListConverter::class)
abstract class HotelHopDatabase : RoomDatabase() {
    abstract val hotelDao: HotelDao
}