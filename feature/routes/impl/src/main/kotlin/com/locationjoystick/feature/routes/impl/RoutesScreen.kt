package com.locationjoystick.feature.routes.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.locationjoystick.core.ui.component.EmptyState

@Composable
fun RoutesRoute(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    viewModel: RoutesViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RoutesScreen(
        uiState = uiState,
        onNavigateToDetail = onNavigateToDetail,
        onNavigateToCreate = onNavigateToCreate,
        onDeleteRoute = viewModel::deleteRoute
    )
}

@Composable
internal fun RoutesScreen(
    uiState: RoutesUiState,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onDeleteRoute: (String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            uiState.routes.isEmpty() -> {
                EmptyState(
                    icon = Icons.Rounded.Add,
                    message = "No routes yet",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(
                        items = uiState.routes,
                        key = { it.id }
                    ) { route ->
                        RouteCard(
                            route = route,
                            onTap = { onNavigateToDetail(route.id) },
                            onDelete = { onDeleteRoute(route.id) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onNavigateToCreate,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Create route")
        }
    }
}

@Composable
private fun RouteCard(
    route: com.locationjoystick.core.model.Route,
    onTap: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(bottom = 12.dp)
    ) {
        Text(route.name)
        Text("${route.waypoints.size} waypoints")
    }
}
