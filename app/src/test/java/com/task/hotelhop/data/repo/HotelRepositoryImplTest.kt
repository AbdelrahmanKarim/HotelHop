package com.task.hotelhop.data.repo

import com.task.hotelhop.data.datasource.hotel.LocalHotelDataSource
import com.task.hotelhop.data.datasource.hotel.RemoteHotelDataSource
import com.task.hotelhop.data.remote.dto.HotelDetailsWrapperDto
import com.task.hotelhop.data.remote.dto.HotelsListResponseDto
import com.task.hotelhop.domain.exception.AppException
import com.task.hotelhop.testutil.testHotelDetailsDto
import com.task.hotelhop.testutil.testHotelDto
import com.task.hotelhop.testutil.testHotelEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HotelRepositoryImplTest {

    private val remoteDS: RemoteHotelDataSource = mockk()
    private val localDS: LocalHotelDataSource = mockk(relaxUnitFun = true)
    private val repository = HotelRepositoryImpl(remoteDS, localDS)

    @Test
    fun refresh_success_replacesNonFavoriteCacheAndKeepsHearts() = runTest {
        val remote = listOf(
            testHotelDto("fav", name = "Nile Palace"),
            testHotelDto("new", name = "New Stay")
        )
        coEvery { remoteDS.getHotels(20, 0) } returns Result.success(HotelsListResponseDto(remote))
        coEvery { localDS.getCachedHotels() } returns flowOf(
            listOf(testHotelEntity("fav", isFavorite = true))
        )

        val saved = repository.refreshHotels(limit = 20, offset = 0)

        assertEquals(2, saved)
        coVerify {
            localDS.replaceNonFavoriteCache(
                match { hotels ->
                    hotels.size == 2 &&
                        hotels.first { it.id == "fav" }.isFavorite &&
                        !hotels.first { it.id == "new" }.isFavorite
                }
            )
        }
        coVerify(exactly = 0) { localDS.cacheHotels(any()) }
    }

    @Test
    fun refresh_success_appendsWhenOffsetIsNotZero() = runTest {
        coEvery { remoteDS.getHotels(20, 20) } returns Result.success(
            HotelsListResponseDto(listOf(testHotelDto("page-2")))
        )
        coEvery { localDS.getCachedHotels() } returns flowOf(emptyList())

        val saved = repository.refreshHotels(limit = 20, offset = 20)

        assertEquals(1, saved)
        coVerify { localDS.cacheHotels(match { it.single().id == "page-2" }) }
        coVerify(exactly = 0) { localDS.replaceNonFavoriteCache(any()) }
    }

    @Test
    fun refresh_networkFailure_withEmptyCache_throwsOfflineAndNoCache() = runTest {
        coEvery { remoteDS.getHotels(20, 0) } returns Result.failure(AppException.NetworkException())
        coEvery { localDS.getCachedHotels() } returns flowOf(emptyList())

        val error = runCatching { repository.refreshHotels(20, 0) }.exceptionOrNull()

        assertTrue(error is AppException.OfflineAndNoCacheException)
        coVerify(exactly = 0) { localDS.replaceNonFavoriteCache(any()) }
    }

    @Test
    fun refresh_networkFailure_withCache_rethrowsNetworkError() = runTest {
        coEvery { remoteDS.getHotels(20, 0) } returns Result.failure(AppException.NetworkException())
        coEvery { localDS.getCachedHotels() } returns flowOf(listOf(testHotelEntity("cached")))

        val error = runCatching { repository.refreshHotels(20, 0) }.exceptionOrNull()

        assertTrue(error is AppException.NetworkException)
    }

    @Test
    fun getHotels_mapsCachedEntitiesToDomain() = runTest {
        coEvery { localDS.getCachedHotels() } returns flowOf(
            listOf(testHotelEntity("cached", name = "Cached Nile", isFavorite = true))
        )

        val hotels = repository.getHotels().first()

        assertEquals(1, hotels.size)
        assertEquals("cached", hotels.first().id)
        assertEquals("Cached Nile", hotels.first().name)
        assertTrue(hotels.first().isFavorite)
    }

    @Test
    fun getHotelDetails_prefersRemoteWhenNetworkSucceeds() = runTest {
        coEvery { remoteDS.getHotelDetails("h1") } returns Result.success(
            HotelDetailsWrapperDto(testHotelDetailsDto("h1", name = "Remote Hotel"))
        )

        val hotel = repository.getHotelDetails("h1")

        assertEquals("Remote Hotel", hotel.name)
        coVerify(exactly = 0) { localDS.getHotelById(any()) }
    }

    @Test
    fun getHotelDetails_fallsBackToCacheWhenNetworkFails() = runTest {
        coEvery { remoteDS.getHotelDetails("h1") } returns Result.failure(AppException.NetworkException())
        coEvery { localDS.getHotelById("h1") } returns testHotelEntity("h1", name = "Cached Hotel")

        val hotel = repository.getHotelDetails("h1")

        assertEquals("Cached Hotel", hotel.name)
        assertFalse(hotel.isFavorite)
    }

    @Test
    fun getHotelDetails_rethrows_whenNetworkFailsAndCacheMisses() = runTest {
        coEvery { remoteDS.getHotelDetails("missing") } returns Result.failure(AppException.NetworkException())
        coEvery { localDS.getHotelById("missing") } returns null

        val error = runCatching { repository.getHotelDetails("missing") }.exceptionOrNull()

        assertTrue(error is AppException.NetworkException)
    }
}
