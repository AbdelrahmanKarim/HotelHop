package com.task.hotelhop.presentation.util

import com.task.hotelhop.R
import com.task.hotelhop.domain.exception.AppException

fun Throwable.toUiText(): UiText {
    return when (this) {
        is AppException.NetworkException -> UiText.StringResource(R.string.error_network)
        is AppException.TimeoutException -> UiText.StringResource(R.string.error_timeout)
        is AppException.ServerException -> UiText.StringResource(R.string.error_server)
        is AppException.OfflineAndNoCacheException -> UiText.StringResource(R.string.error_offline_no_cache)
        is AppException.EmptySearchResultsException -> UiText.StringResource(R.string.error_empty_search)
        is AppException.InvalidBookingDateException -> UiText.StringResource(R.string.error_invalid_booking_date)
        is AppException.InvalidRoomCountException -> UiText.StringResource(R.string.error_invalid_room_count)
        is AppException.InvalidCredentialsException -> UiText.StringResource(R.string.error_invalid_credentials)
        is AppException.UserAlreadyExistsException -> UiText.StringResource(R.string.error_user_exists)
        is AppException.UnknownException -> UiText.StringResource(R.string.error_unknown)
        else -> UiText.StringResource(R.string.error_unknown)
    }
}
