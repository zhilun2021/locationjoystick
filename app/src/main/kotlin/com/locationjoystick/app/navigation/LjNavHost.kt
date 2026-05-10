package com.locationjoystick.app.navigation

import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.locationjoystick.feature.favorites.api.FAVORITES_ROUTE
import com.locationjoystick.feature.favorites.impl.FavoritesRoute
import com.locationjoystick.feature.map.api.MAP_ROUTE
import com.locationjoystick.feature.map.impl.MapRoute
import com.locationjoystick.feature.roaming.api.ROAMING_ROUTE
import com.locationjoystick.feature.roaming.impl.RoamingRoute
import com.locationjoystick.feature.routes.api.ROUTES_ROUTE
import com.locationjoystick.feature.routes.impl.RoutesRoute
import com.locationjoystick.feature.settings.api.SETTINGS_ROUTE
import com.locationjoystick.feature.settings.impl.SettingsRoute
import com.locationjoystick.feature.setup.api.SETUP_ROUTE
import com.locationjoystick.feature.setup.impl.SetupRoute

@Composable
fun LjNavHost(
    navController: NavHostController,
    drawerState: DrawerState
) {
    NavHost(
        navController = navController,
        startDestination = SETUP_ROUTE
    ) {
        composable(SETUP_ROUTE) {
            SetupRoute(
                onSetupComplete = {
                    navController.navigate(MAP_ROUTE) {
                        popUpTo(SETUP_ROUTE) { inclusive = true }
                    }
                }
            )
        }

        composable(MAP_ROUTE) {
            MapRoute(
                drawerState = drawerState
            )
        }

        composable(ROUTES_ROUTE) {
            RoutesRoute(
                onNavigateToDetail = { routeId ->
                    // Navigate to detail
                },
                onNavigateToCreate = {
                    // Navigate to create
                },
                viewModel = androidx.hilt.navigation.compose.hiltViewModel()
            )
        }

        composable(FAVORITES_ROUTE) {
            FavoritesRoute(
                viewModel = androidx.hilt.navigation.compose.hiltViewModel()
            )
        }

        composable(ROAMING_ROUTE) {
            RoamingRoute(
                viewModel = androidx.hilt.navigation.compose.hiltViewModel()
            )
        }

        composable(SETTINGS_ROUTE) {
            SettingsRoute(
                viewModel = androidx.hilt.navigation.compose.hiltViewModel()
            )
        }
    }
}
