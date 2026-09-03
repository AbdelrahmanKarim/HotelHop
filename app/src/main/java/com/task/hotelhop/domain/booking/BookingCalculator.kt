package com.task.hotelhop.domain.booking

import com.task.hotelhop.domain.exception.AppException
import java.util.concurrent.TimeUnit

data class BookingQuote(
    val nights: Int,
    val roomCount: Int,
    val subtotal: Double,
    val vat: Double,
    val total: Double
)

object BookingCalculator {
    const val VAT_RATE = 0.15
    const val MIN_ROOMS = 1
    const val MAX_ROOMS = 10

    fun nights(checkInMillis: Long?, checkOutMillis: Long?): Int {
        if (checkInMillis == null || checkOutMillis == null || checkOutMillis <= checkInMillis) {
            return 0
        }
        return TimeUnit.MILLISECONDS.toDays(checkOutMillis - checkInMillis).toInt().coerceAtLeast(1)
    }

    fun quote(pricePerNight: Double, nights: Int, roomCount: Int): BookingQuote {
        val rooms = coerceRooms(roomCount)
        val billedNights = nights.coerceAtLeast(0)
        val subtotal = pricePerNight * billedNights * rooms
        val vat = subtotal * VAT_RATE
        return BookingQuote(
            nights = billedNights,
            roomCount = rooms,
            subtotal = subtotal,
            vat = vat,
            total = subtotal + vat
        )
    }

    fun dateViolation(
        checkInMillis: Long?,
        checkOutMillis: Long?,
        todayMillis: Long
    ): AppException? {
        if (checkInMillis == null && checkOutMillis == null) return null
        if ((checkInMillis != null && checkInMillis < todayMillis) ||
            (checkOutMillis != null && checkOutMillis < todayMillis)
        ) {
            return AppException.PastBookingDateException()
        }
        if (checkInMillis != null && checkOutMillis != null && checkOutMillis <= checkInMillis) {
            return AppException.InvalidBookingDateException()
        }
        return null
    }

    fun coerceRooms(count: Int): Int = count.coerceIn(MIN_ROOMS, MAX_ROOMS)
}
