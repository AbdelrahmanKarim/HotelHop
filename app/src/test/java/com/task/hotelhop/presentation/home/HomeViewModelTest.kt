package com.task.hotelhop.presentation.home

import com.task.hotelhop.domain.entity.Hotel
import com.task.hotelhop.domain.exception.AppException
import com.task.hotelhop.domain.usecase.hotel.GetPagedHotelsUseCase
import com.task.hotelhop.domain.usecase.hotel.ToggleFavoriteUseCase
import com.task.hotelhop.domain.usecase.user.CheckUserLoggedInUseCase
import com.task.hotelhop.testutil.MainDispatcherRule
import com.task.hotelhop.testutil.testHotel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getPagedHotelsUseCase: GetPagedHotelsUseCase = mockk()
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase = mockk(relaxUnitFun = true)
    private val checkUserLoggedInUseCase: CheckUserLoggedInUseCase = mockk()

    @Test
    fun guestFavorite_showsLoginGateAndDoesNotToggle() = runTest {
        val hotel = testHotel("h1")
        val viewModel = viewModel(
            cached = listOf(hotel),
            loggedIn = false
        )

        viewModel.onEvent(HomeUiEvent.FavoriteToggled(hotel))

        assertTrue(viewModel.uiState.value.showLoginRequired)
        coVerify(exactly = 0) { toggleFavoriteUseCase(any(), any()) }
    }

    @Test
    fun signedInFavorite_addsHeartWithoutConfirmation() = runTest {
        val hotel = testHotel("h1", isFavorite = false)
        val viewModel = viewModel(cached = listOf(hotel), loggedIn = true)

        viewModel.onEvent(HomeUiEvent.FavoriteToggled(hotel))

        assertFalse(viewModel.uiState.value.showLoginRequired)
        assertNull(viewModel.uiState.value.pendingUnfavorite)
        coVerify { toggleFavoriteUseCase(hotel.id, true) }
    }

    @Test
    fun signedInUnfavorite_asksForConfirmationThenRemoves() = runTest {
        val hotel = testHotel("h1", isFavorite = true)
        val viewModel = viewModel(cached = listOf(hotel), loggedIn = true)

        viewModel.onEvent(HomeUiEvent.FavoriteToggled(hotel))
        assertEquals(hotel, viewModel.uiState.value.pendingUnfavorite)
        coVerify(exactly = 0) { toggleFavoriteUseCase(any(), any()) }

        viewModel.onEvent(HomeUiEvent.UnfavoriteConfirmed)
        assertNull(viewModel.uiState.value.pendingUnfavorite)
        coVerify { toggleFavoriteUseCase(hotel.id, false) }
    }

    @Test
    fun popularTakesTopRated_andBestPriceExcludesThoseIds() = runTest {
        val hotels = (1..10).map { index ->
            testHotel(
                id = "h$index",
                rating = index.toDouble(),
                pricePerNight = 200.0 - index
            )
        }
        val viewModel = viewModel(cached = hotels, loggedIn = true)

        val popularIds = viewModel.uiState.value.popularHotels.map { it.id }
        val bestPriceIds = viewModel.uiState.value.bestPriceHotels.map { it.id }

        assertEquals(listOf("h10", "h9", "h8", "h7", "h6", "h5", "h4", "h3"), popularIds)
        assertEquals(listOf("h2", "h1"), bestPriceIds)
        assertTrue(bestPriceIds.none { it in popularIds })
    }

    @Test
    fun emptyCacheEmit_whileRefreshing_keepsPreviousHotels() = runTest {
        val cached = testHotel("cached")
        val cache = MutableStateFlow(listOf(cached))
        every { getPagedHotelsUseCase.observeHotels() } returns cache
        every { checkUserLoggedInUseCase() } returns MutableStateFlow(true)
        coEvery { getPagedHotelsUseCase(any(), any()) } returns 1

        val viewModel = HomeViewModel(
            getPagedHotelsUseCase,
            toggleFavoriteUseCase,
            checkUserLoggedInUseCase
        )
        assertEquals(listOf(cached), viewModel.uiState.value.hotels)

        val refreshResult = CompletableDeferred<Int>()
        coEvery { getPagedHotelsUseCase(any(), any()) } coAnswers { refreshResult.await() }
        viewModel.onEvent(HomeUiEvent.Refresh)
        assertTrue(viewModel.uiState.value.isRefreshing)

        cache.value = emptyList()
        assertEquals(listOf(cached), viewModel.uiState.value.hotels)

        refreshResult.complete(1)
        assertEquals(listOf(cached), viewModel.uiState.value.hotels)
        assertFalse(viewModel.uiState.value.isOfflineEmpty)
    }

    @Test
    fun networkFailureWithNoCache_marksOfflineEmpty() = runTest {
        every { getPagedHotelsUseCase.observeHotels() } returns MutableStateFlow(emptyList())
        every { checkUserLoggedInUseCase() } returns MutableStateFlow(true)
        coEvery { getPagedHotelsUseCase(any(), any()) } throws AppException.OfflineAndNoCacheException()

        val viewModel = HomeViewModel(
            getPagedHotelsUseCase,
            toggleFavoriteUseCase,
            checkUserLoggedInUseCase
        )

        assertTrue(viewModel.uiState.value.isOfflineEmpty)
        assertTrue(viewModel.uiState.value.hotels.isEmpty())
    }

    private fun viewModel(cached: List<Hotel>, loggedIn: Boolean): HomeViewModel {
        every { getPagedHotelsUseCase.observeHotels() } returns MutableStateFlow(cached)
        every { checkUserLoggedInUseCase() } returns MutableStateFlow(loggedIn)
        coEvery { getPagedHotelsUseCase(any(), any()) } returns cached.size
        return HomeViewModel(
            getPagedHotelsUseCase,
            toggleFavoriteUseCase,
            checkUserLoggedInUseCase
        )
    }
}
