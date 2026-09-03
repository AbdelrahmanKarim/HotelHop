package com.task.hotelhop.domain.entity

data class Hotel(
    val id: String,
    val name: String,
    val city: String,
    val rating: Double,
    val pricePerNight: Double,
    val mainImage: String,
    val images: List<String> = emptyList(),
    val amenities: List<String> = emptyList(),
    val description: String = "",
    val locationDetails: String = "",
    val locationUrl: String = "",
    val isFavorite: Boolean = false
)