package com.locationjoystick.app.navigation

import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.locationjoystick.feature.favorites.api.FAVORITES_ROUTE
import com.locationjoystick.feature.favorites.api.MAP_PICKER_ROUTE
import com.locationjoystick.feature.favorites.impl.FavoritesRoute
import com.locationjoystick.feature.favorites.impl.MapPickerRoute
import com.locationjoystick.feature.map.api.MAP_ROUTE
import com.locationjoystick.feature.map.impl.MapRoute
import com.locationjoystick.feature.roaming.api.ROAMING_ROUTE
import com.locationjoystick.feature.roaming.impl.RoamingRoute
import com.locationjoystick.feature.routes.api.ROUTES_ROUTE
import com.locationjoystick.feature.routes.api.ROUTE_CREATOR_ROUTE
import com.locationjoystick.feature.routes.api.ROUTE_DETAIL_ROUTE
import com.locationjoystick.feature.routes.impl.RouteCreatorRoute
import com.locationjoystick.feature.routes.impl.RouteDetailScreen
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
                    navController.navigate("$ROUTE_DETAIL_ROUTE/$routeId")
                },
                onNavigateToCreate = {
                    navController.navigate(ROUTE_CREATOR_ROUTE)
                },
                viewModel = androidx.hilt.navigation.compose.hiltViewModel()
            )
        }

        composable(ROUTE_CREATOR_ROUTE) {
            RouteCreatorRoute(
                onRouteSaved = {
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("$ROUTE_DETAIL_ROUTE/{routeId}") { backStackEntry ->
            val routeId = backStackEntry.arguments?.getString("routeId") ?: return@composable
            RouteDetailScreen(routeId = routeId)
        }

        composable(FAVORITES_ROUTE) {
            FavoritesRoute(
                viewModel = androidx.hilt.navigation.compose.hiltViewModel()
            )
        }

        composable(MAP_PICKER_ROUTE) {
            MapPickerRoute(
                onLocationPicked = { lat, lon ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("picked_lat", lat)
                    navController.previousBackStackEntry?.savedStateHandle?.set("picked_lon", lon)
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
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
