package com.example.atmos.data.util

import android.database.sqlite.SQLiteException
import coil3.network.HttpException
import com.task.hotelhop.domain.exception.AppException
import java.io.IOException
import java.net.SocketTimeoutException

fun Throwable.toAppException(): AppException {
    return when (this) {
        is AppException -> this
        is SocketTimeoutException -> AppException.TimeoutException()
        is IOException -> AppException.NetworkException()
        is HttpException -> AppException.ServerException()
        is SQLiteException -> AppException.UnknownException()
        else -> AppException.UnknownException()
    }
}

suspend fun <T> safeCall(call: suspend () -> T): Result<T> {
    return try {
        Result.success(call())
    } catch (e: Exception) {
        Result.failure(e.toAppException())
    }
}