package com.locationjoystick.app.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.locationjoystick.feature.favorites.api.FAVORITES_ROUTE
import com.locationjoystick.feature.map.api.MAP_ROUTE
import com.locationjoystick.feature.roaming.api.ROAMING_ROUTE
import com.locationjoystick.feature.routes.api.ROUTES_ROUTE
import com.locationjoystick.feature.settings.api.SETTINGS_ROUTE
import kotlinx.coroutines.launch

@Composable
fun LjDrawerContent(
    navController: NavHostController,
    drawerState: DrawerState
) {
    val scope = rememberCoroutineScope()
    val destinations = listOf(
        TopLevelDestination(
            route = MAP_ROUTE,
            icon = Icons.Rounded.Map,
            label = "Map"
        ),
        TopLevelDestination(
            route = ROUTES_ROUTE,
            icon = Icons.Rounded.Route,
            label = "Routes"
        ),
        TopLevelDestination(
            route = FAVORITES_ROUTE,
            icon = Icons.Rounded.Favorite,
            label = "Favorites"
        ),
        TopLevelDestination(
            route = ROAMING_ROUTE,
            icon = Icons.Rounded.Explore,
            label = "Roaming"
        ),
        TopLevelDestination(
            route = SETTINGS_ROUTE,
            icon = Icons.Rounded.Settings,
            label = "Settings"
        ),
    )

    ModalDrawerSheet {
        Spacer(modifier = Modifier.height(16.dp))
        destinations.forEach { destination ->
            NavigationDrawerItem(
                icon = {
                    Icon(
                        destination.icon,
                        contentDescription = destination.label
                    )
                },
                label = { Text(destination.label) },
                selected = false,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(MAP_ROUTE) { inclusive = false }
                    }
                    scope.launch {
                        drawerState.close()
                    }
                }
            )
        }
    }
}

data class TopLevelDestination(
    val route: String,
    val icon: androidx.compose.material.icons.Types.ImageVector,
    val label: String
)
