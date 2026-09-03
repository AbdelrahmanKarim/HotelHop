package com.task.hotelhop.testutil

import com.task.hotelhop.data.local.entity.HotelEntity
import com.task.hotelhop.data.remote.dto.HotelDetailsDto
import com.task.hotelhop.data.remote.dto.HotelDto
import com.task.hotelhop.domain.entity.Hotel

fun testHotel(
    id: String,
    name: String = "Hotel $id",
    city: String = "Cairo",
    rating: Double = 4.2,
    pricePerNight: Double = 120.0,
    isFavorite: Boolean = false
) = Hotel(
    id = id,
    name = name,
    city = city,
    rating = rating,
    pricePerNight = pricePerNight,
    mainImage = "",
    isFavorite = isFavorite
)

fun testHotelEntity(
    id: String,
    name: String = "Hotel $id",
    city: String = "Cairo",
    rating: Double = 4.2,
    pricePerNight: Double = 120.0,
    isFavorite: Boolean = false
) = HotelEntity(
    id = id,
    name = name,
    city = city,
    rating = rating,
    pricePerNight = pricePerNight,
    mainImage = "",
    images = emptyList(),
    amenities = emptyList(),
    description = "",
    locationDetails = "",
    locationUrl = "",
    isFavorite = isFavorite
)

fun testHotelDto(
    id: String,
    name: String = "Hotel $id",
    city: String = "Cairo",
    stars: Int = 3,
    rating: Double = 4.2
) = HotelDto(
    id = id,
    name = name,
    city = city,
    hotelDescription = "A stay in $city",
    main_photo = null,
    stars = stars,
    rating = rating
)

fun testHotelDetailsDto(
    id: String,
    name: String = "Hotel $id",
    city: String = "Cairo"
) = HotelDetailsDto(
    id = id,
    name = name,
    city = city,
    hotelDescription = "Details for $name",
    starRating = 4,
    rating = 4.5
)
