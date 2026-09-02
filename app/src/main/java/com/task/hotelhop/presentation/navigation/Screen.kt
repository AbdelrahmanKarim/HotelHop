package com.task.hotelhop.presentation.navigation

import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.task.hotelhop.R

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Home : Screen("home")
    data object Search : Screen("search")
    data object Favorites : Screen("favorites")
    data object Account : Screen("account")
    data object HotelDetails : Screen("hotel_details/{hotelId}") {
        const val ARG_HOTEL_ID = "hotelId"
        fun createRoute(hotelId: String) = "hotel_details/${Uri.encode(hotelId)}"
    }
    data object Checkout : Screen("checkout/{hotelId}") {
        const val ARG_HOTEL_ID = "hotelId"
        fun createRoute(hotelId: String) = "checkout/${Uri.encode(hotelId)}"
    }
}

data class BottomNavDestination(
    val screen: Screen,
    @StringRes val titleResId: Int,
    val icon: ImageVector
)

val bottomNavDestinations = listOf(
    BottomNavDestination(Screen.Home, R.string.nav_home, Icons.Filled.Home),
    BottomNavDestination(Screen.Search, R.string.nav_search, Icons.Filled.Search),
    BottomNavDestination(Screen.Favorites, R.string.nav_favorites, Icons.Filled.Favorite),
    BottomNavDestination(Screen.Account, R.string.nav_account, Icons.Filled.AccountCircle)
)
