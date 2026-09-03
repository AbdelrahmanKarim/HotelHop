package com.task.hotelhop.presentation.util

import android.os.SystemClock
import androidx.navigation.NavController

private var lastNavigateAt = 0L
private var lastNavigatedRoute: String? = null

fun NavController.navigateOnce(route: String) {
    val now = SystemClock.elapsedRealtime()
    if (route == lastNavigatedRoute && now - lastNavigateAt < NAVIGATION_THROTTLE_MS) return
    if (currentDestination?.route == route) return
    lastNavigatedRoute = route
    lastNavigateAt = now
    navigate(route) {
        launchSingleTop = true
    }
}

private const val NAVIGATION_THROTTLE_MS = 800L
