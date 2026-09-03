package com.task.hotelhop.presentation.util

import java.time.LocalDate
import java.time.ZoneOffset

fun startOfTodayUtc(): Long {
    return LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}

const val DAY_IN_MILLIS = 24L * 60L * 60L * 1000L
