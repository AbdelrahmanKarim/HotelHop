package com.task.hotelhop.presentation.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.task.hotelhop.presentation.design_system.theme.HotelHopTheme

@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val colors = HotelHopTheme.colors

    NavigationBar(
        containerColor = colors.surfaceLow
    ) {
        bottomNavDestinations.forEach { destination ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == destination.screen.route } == true
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(destination.screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = stringResource(destination.titleResId)
                    )
                },
                label = {
                    Text(
                        text = stringResource(destination.titleResId),
                        style = HotelHopTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = colors.primary.copy(alpha = 0.16f),
                    selectedIconColor = colors.primary,
                    selectedTextColor = colors.primary,
                    unselectedIconColor = colors.textBody,
                    unselectedTextColor = colors.textBody
                )
            )
        }
    }
}
