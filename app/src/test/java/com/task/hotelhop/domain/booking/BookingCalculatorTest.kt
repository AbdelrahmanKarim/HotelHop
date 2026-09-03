package com.task.hotelhop.domain.booking

import com.task.hotelhop.domain.exception.AppException
import com.task.hotelhop.presentation.util.DAY_IN_MILLIS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BookingCalculatorTest {

    private val today = 1_725_321_600_000L

    @Test
    fun nights_areZero_whenEitherDateIsMissing() {
        assertEquals(0, BookingCalculator.nights(null, today + DAY_IN_MILLIS))
        assertEquals(0, BookingCalculator.nights(today, null))
        assertEquals(0, BookingCalculator.nights(null, null))
    }

    @Test
    fun nights_areZero_whenCheckoutIsNotAfterCheckIn() {
        assertEquals(0, BookingCalculator.nights(today, today))
        assertEquals(0, BookingCalculator.nights(today + DAY_IN_MILLIS, today))
    }

    @Test
    fun nights_countWholeDaysBetweenValidDates() {
        assertEquals(1, BookingCalculator.nights(today, today + DAY_IN_MILLIS))
        assertEquals(3, BookingCalculator.nights(today, today + 3 * DAY_IN_MILLIS))
    }

    @Test
    fun nights_roundPartialDayUpToAtLeastOne() {
        assertEquals(1, BookingCalculator.nights(today, today + DAY_IN_MILLIS / 2))
    }

    @Test
    fun quote_appliesFifteenPercentVatAndRoomMultiplier() {
        val quote = BookingCalculator.quote(pricePerNight = 100.0, nights = 2, roomCount = 3)

        assertEquals(2, quote.nights)
        assertEquals(3, quote.roomCount)
        assertEquals(600.0, quote.subtotal, 0.0)
        assertEquals(90.0, quote.vat, 0.0)
        assertEquals(690.0, quote.total, 0.0)
    }

    @Test
    fun quote_isZero_whenThereAreNoNights() {
        val quote = BookingCalculator.quote(pricePerNight = 200.0, nights = 0, roomCount = 2)

        assertEquals(0.0, quote.subtotal, 0.0)
        assertEquals(0.0, quote.vat, 0.0)
        assertEquals(0.0, quote.total, 0.0)
    }

    @Test
    fun quote_clampsRoomsBetweenOneAndTen() {
        assertEquals(1, BookingCalculator.quote(100.0, 1, 0).roomCount)
        assertEquals(10, BookingCalculator.quote(100.0, 1, 20).roomCount)
        assertEquals(100.0, BookingCalculator.quote(100.0, 1, 0).subtotal, 0.0)
        assertEquals(1000.0, BookingCalculator.quote(100.0, 1, 20).subtotal, 0.0)
    }

    @Test
    fun dateViolation_isNull_whenNoDatesAreChosen() {
        assertNull(BookingCalculator.dateViolation(null, null, today))
    }

    @Test
    fun dateViolation_rejectsPastCheckInOrCheckout() {
        val pastCheckIn = BookingCalculator.dateViolation(today - DAY_IN_MILLIS, today + DAY_IN_MILLIS, today)
        val pastCheckOut = BookingCalculator.dateViolation(today, today - DAY_IN_MILLIS, today)

        assertTrue(pastCheckIn is AppException.PastBookingDateException)
        assertTrue(pastCheckOut is AppException.PastBookingDateException)
    }

    @Test
    fun dateViolation_rejectsCheckoutOnOrBeforeCheckIn() {
        val sameDay = BookingCalculator.dateViolation(today, today, today)
        val reversed = BookingCalculator.dateViolation(today + DAY_IN_MILLIS, today, today)

        assertTrue(sameDay is AppException.InvalidBookingDateException)
        assertTrue(reversed is AppException.InvalidBookingDateException)
    }

    @Test
    fun dateViolation_allowsTodayAndALaterCheckout() {
        assertNull(BookingCalculator.dateViolation(today, today + DAY_IN_MILLIS, today))
    }
}
