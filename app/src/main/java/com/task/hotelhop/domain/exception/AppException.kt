package com.task.hotelhop.domain.exception


sealed class AppException(errorMessage: String, cause: Throwable? = null) : Exception(errorMessage, cause) {

    class NetworkException : AppException("Oops! It looks like you're offline. Please check your internet connection.")
    class TimeoutException : AppException("The connection took too long. Please try again.")
    class ServerException : AppException("We're having trouble reaching our servers right now. Please try again later.")
    class OfflineAndNoCacheException : AppException("You are offline and no saved hotels are available to display.")
    class EmptySearchResultsException : AppException("No hotels found matching your search or filters.")
    class InvalidBookingDateException : AppException("Check-out date cannot be on or before the check-in date.")
    class PastBookingDateException : AppException("Booking dates cannot be in the past.")
    class InvalidRoomCountException : AppException("Please select at least one room to proceed.")
    class InvalidCredentialsException : AppException("The email or password you entered is incorrect.")
    class UserAlreadyExistsException : AppException("An account with this email already exists.")
    class UnknownException : AppException("Something went wrong on our end. Please restart the app and try again.")
}