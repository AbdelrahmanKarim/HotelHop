package com.task.hotelhop.presentation.checkout

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.task.hotelhop.R
import com.task.hotelhop.domain.usecase.hotel.GetHotelDetailsUseCase
import com.task.hotelhop.domain.usecase.payment.CreateCardPaymentUseCase
import com.task.hotelhop.domain.usecase.user.GetUserDetailsUseCase
import com.task.hotelhop.presentation.navigation.Screen
import com.task.hotelhop.presentation.util.DAY_IN_MILLIS
import com.task.hotelhop.presentation.util.startOfTodayUtc
import com.task.hotelhop.testutil.MainDispatcherRule
import com.task.hotelhop.testutil.assertStringRes
import com.task.hotelhop.testutil.testHotel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CheckoutViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getHotelDetailsUseCase: GetHotelDetailsUseCase = mockk()
    private val getUserDetailsUseCase: GetUserDetailsUseCase = mockk(relaxed = true)
    private val createCardPaymentUseCase: CreateCardPaymentUseCase = mockk(relaxed = true)
    private val hotel = testHotel(id = "h1", pricePerNight = 100.0)

    @Before
    fun setUp() {
        coEvery { getHotelDetailsUseCase(hotel.id) } returns hotel
    }

    @Test
    fun loadsHotelAndLeavesTotalsAtZeroUntilDatesAreChosen() = runTest {
        val viewModel = viewModel()

        assertEquals(hotel, viewModel.uiState.value.hotel)
        assertEquals(0, viewModel.uiState.value.nights)
        assertEquals(0.0, viewModel.uiState.value.total, 0.0)
    }

    @Test
    fun validDatesAndTwoRooms_updateNightsVatAndTotal() = runTest {
        val viewModel = viewModel()
        val today = startOfTodayUtc()

        viewModel.onEvent(CheckoutUiEvent.CheckInSelected(today))
        viewModel.onEvent(CheckoutUiEvent.CheckOutSelected(today + 2 * DAY_IN_MILLIS))
        viewModel.onEvent(CheckoutUiEvent.IncrementRooms)

        val state = viewModel.uiState.value
        assertEquals(2, state.nights)
        assertEquals(2, state.roomCount)
        assertEquals(400.0, state.subtotal, 0.0)
        assertEquals(60.0, state.vat, 0.0)
        assertEquals(460.0, state.total, 0.0)
        assertNull(state.dateError)
    }

    @Test
    fun pastCheckIn_setsPastDateError() = runTest {
        val viewModel = viewModel()

        viewModel.onEvent(CheckoutUiEvent.CheckInSelected(startOfTodayUtc() - DAY_IN_MILLIS))

        viewModel.uiState.value.dateError.assertStringRes(R.string.error_invalid_booking_past)
    }

    @Test
    fun checkoutOnOrBeforeCheckIn_clearsCheckoutAndLeavesNoDateError() = runTest {
        val viewModel = viewModel()
        val today = startOfTodayUtc()

        viewModel.onEvent(CheckoutUiEvent.CheckInSelected(today + DAY_IN_MILLIS))
        viewModel.onEvent(CheckoutUiEvent.CheckOutSelected(today + DAY_IN_MILLIS))

        assertNull(viewModel.uiState.value.checkOutMillis)
        assertNull(viewModel.uiState.value.dateError)
        assertEquals(0, viewModel.uiState.value.nights)
    }

    @Test
    fun confirmWithoutDates_setsInvalidDateError() = runTest {
        val viewModel = viewModel()

        viewModel.onEvent(CheckoutUiEvent.ConfirmBooking)

        viewModel.uiState.value.dateError.assertStringRes(R.string.error_invalid_booking_date)
        assertNull(viewModel.uiState.value.bookingReference)
    }

    @Test
    fun roomsStayBetweenOneAndTen() = runTest {
        val viewModel = viewModel()

        repeat(12) { viewModel.onEvent(CheckoutUiEvent.IncrementRooms) }
        assertEquals(10, viewModel.uiState.value.roomCount)

        repeat(20) { viewModel.onEvent(CheckoutUiEvent.DecrementRooms) }
        assertEquals(1, viewModel.uiState.value.roomCount)
    }

    @Test
    fun cashConfirm_withValidDates_createsBookingReference() = runTest {
        val viewModel = viewModel()
        val today = startOfTodayUtc()
        viewModel.onEvent(CheckoutUiEvent.CheckInSelected(today))
        viewModel.onEvent(CheckoutUiEvent.CheckOutSelected(today + DAY_IN_MILLIS))

        viewModel.onEvent(CheckoutUiEvent.ConfirmBooking)

        val reference = viewModel.uiState.value.bookingReference
        assertNotNull(reference)
        assertTrue(reference!!.startsWith("HH-"))
    }

    @Test
    fun backClicked_emitsNavigateBack() = runTest {
        val viewModel = viewModel()

        viewModel.effect.test {
            viewModel.onEvent(CheckoutUiEvent.BackClicked)
            assertEquals(CheckoutUiEffect.NavigateBack, awaitItem())
        }
    }

    private fun viewModel() = CheckoutViewModel(
        savedStateHandle = SavedStateHandle(mapOf(Screen.Checkout.ARG_HOTEL_ID to hotel.id)),
        getHotelDetailsUseCase = getHotelDetailsUseCase,
        getUserDetailsUseCase = getUserDetailsUseCase,
        createCardPaymentUseCase = createCardPaymentUseCase
    )
}
