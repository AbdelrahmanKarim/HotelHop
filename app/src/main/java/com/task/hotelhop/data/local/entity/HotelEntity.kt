package com.task.hotelhop.data.local.entity


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hotels")
data class HotelEntity(
    @PrimaryKey val id: String,
    val name: String,
    val city: String,
    val rating: Double,
    val pricePerNight: Double,
    val mainImage: String,
    val images: List<String>,
    val amenities: List<String>,
    val description: String,
    val locationDetails: String,
    val locationUrl: String,
    val isFavorite: Boolean
)