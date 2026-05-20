package com.locationjoystick.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.locationjoystick.core.model.FavoriteLocation
import com.locationjoystick.core.model.LatLng

/**
 * Shared composable for displaying a list of favorite locations.
 *
 * @param title Header text (e.g. "Favorites", "Jump to Favorite").
 * @param favorites List of [FavoriteLocation] items to display.
 * @param onSelect Called with the selected [FavoriteLocation].
 * @param onSaveCurrentLocation Optional: when non-null, an Add icon button is shown in the header.
 */
@Composable
fun FavoritesList(
    title: String,
    favorites: List<FavoriteLocation>,
    onSelect: (FavoriteLocation) -> Unit,
    modifier: Modifier = Modifier,
    onSaveCurrentLocation: (() -> Unit)? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            if (onSaveCurrentLocation != null) {
                IconButton(onClick = onSaveCurrentLocation) {
                    Icon(Icons.Default.Add, contentDescription = "Save current location")
                }
            }
        }

        if (favorites.isEmpty()) {
            Text(
                "No saved favorites yet",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp),
            )
        } else {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items = favorites, key = { it.id }) { favorite ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(8.dp),
                                ).clickable { onSelect(favorite) }
                                .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(favorite.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${String.format("%.4f", favorite.position.latitude)}, " +
                                    "${String.format("%.4f", favorite.position.longitude)}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoritesListEmptyPreview() {
    FavoritesList(
        title = "Favorites",
        favorites = emptyList(),
        onSelect = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun FavoritesListWithItemsPreview() {
    FavoritesList(
        title = "Favorites",
        favorites =
            listOf(
                FavoriteLocation(
                    id = "1",
                    name = "Home",
                    position = LatLng(48.8566, 2.3522),
                    createdAt = 0L,
                ),
                FavoriteLocation(
                    id = "2",
                    name = "Work",
                    position = LatLng(48.8606, 2.3376),
                    createdAt = 0L,
                ),
            ),
        onSelect = {},
        onSaveCurrentLocation = {},
    )
}
