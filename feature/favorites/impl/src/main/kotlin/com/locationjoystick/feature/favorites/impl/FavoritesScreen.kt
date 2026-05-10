package com.locationjoystick.feature.favorites.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
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
internal fun FavoritesRoute(
    viewModel: FavoritesViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FavoritesScreen(
        uiState = uiState,
        onTeleport = viewModel::teleportTo,
        onDelete = { viewModel.deleteFavorite(it.id) },
        onRename = viewModel::renameFavorite
    )
}

@Composable
internal fun FavoritesScreen(
    uiState: FavoritesUiState,
    onTeleport: (com.locationjoystick.core.model.FavoriteLocation) -> Unit,
    onDelete: (com.locationjoystick.core.model.FavoriteLocation) -> Unit,
    onRename: (String, String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            uiState.favorites.isEmpty() -> {
                EmptyState(
                    icon = Icons.Rounded.LocationOn,
                    message = "No saved locations yet",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(
                        items = uiState.favorites,
                        key = { it.id }
                    ) { favorite ->
                        FavoriteCard(
                            favorite = favorite,
                            onTeleport = { onTeleport(favorite) },
                            onDelete = { onDelete(favorite) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            enabled = false
        ) {
            Icon(Icons.Rounded.LocationOn, contentDescription = "Save location")
        }
    }
}

@Composable
private fun FavoriteCard(
    favorite: com.locationjoystick.core.model.FavoriteLocation,
    onTeleport: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(bottom = 12.dp)
    ) {
        Text(favorite.name)
        Text("${String.format("%.4f", favorite.lat)}, ${String.format("%.4f", favorite.lon)}")
    }
}
