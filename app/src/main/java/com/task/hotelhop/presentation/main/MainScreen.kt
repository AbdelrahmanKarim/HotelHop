package com.task.hotelhop.presentation.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.task.hotelhop.presentation.checkout.CheckoutScreen
import com.task.hotelhop.presentation.design_system.theme.HotelHopTheme
import com.task.hotelhop.presentation.favorite.FavoriteScreen
import com.task.hotelhop.presentation.home.HomeScreen
import com.task.hotelhop.presentation.hotel_details.HotelDetailsScreen
import com.task.hotelhop.presentation.login.LoginScreen
import com.task.hotelhop.presentation.navigation.BottomNavBar
import com.task.hotelhop.presentation.navigation.Screen
import com.task.hotelhop.presentation.navigation.bottomNavDestinations
import com.task.hotelhop.presentation.on_boarding.OnboardingScreen
import com.task.hotelhop.presentation.register.RegisterScreen
import com.task.hotelhop.presentation.search.SearchScreen
import com.task.hotelhop.presentation.splash.SplashScreen
import com.task.hotelhop.presentation.util.applyAppLanguage
import com.task.hotelhop.presentation.util.findActivityOrNull
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDarkTheme = uiState.isDarkTheme ?: isSystemInDarkTheme()

    val view = LocalView.current
    LaunchedEffect(uiState.languageCode) {
        val languageCode = uiState.languageCode ?: return@LaunchedEffect
        view.context.findActivityOrNull()?.applyAppLanguage(languageCode)
    }

    HotelHopTheme(isDarkTheme = isDarkTheme) {
        StatusBarIconColor(darkIcons = !isDarkTheme)
        MainScaffold()
    }
}

@Composable
private fun MainScaffold() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val bottomBarRoutes = bottomNavDestinations.map { it.screen.route }
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(innerPadding)
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            HotelHopNavGraph(navController = navController)
        }
    }
}

@Composable
private fun HotelHopNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = { navController.navigateToHome() }
            )
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToHome = { navController.navigateToHome() },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToHome = { navController.navigateToHome() },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToDetails = { hotelId ->
                    navController.navigate(Screen.HotelDetails.createRoute(hotelId))
                }
            )
        }
        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateToDetails = { hotelId ->
                    navController.navigate(Screen.HotelDetails.createRoute(hotelId))
                }
            )
        }
        composable(Screen.Favorites.route) {
            FavoriteScreen(
                onNavigateToDetails = { hotelId ->
                    navController.navigate(Screen.HotelDetails.createRoute(hotelId))
                }
            )
        }
        composable(
            route = Screen.HotelDetails.route,
            arguments = listOf(navArgument(Screen.HotelDetails.ARG_HOTEL_ID) { type = NavType.StringType })
        ) {
            HotelDetailsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCheckout = { hotelId ->
                    navController.navigate(Screen.Checkout.createRoute(hotelId))
                }
            )
        }
        composable(
            route = Screen.Checkout.route,
            arguments = listOf(navArgument(Screen.Checkout.ARG_HOTEL_ID) { type = NavType.StringType })
        ) {
            CheckoutScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}

private fun NavHostController.navigateToHome() {
    navigate(Screen.Home.route) {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }
}

@Composable
private fun StatusBarIconColor(darkIcons: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivityOrNull()?.window ?: return@SideEffect
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = darkIcons
        }
    }
}
