package com.locationjoystick.feature.favorites.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.locationjoystick.core.data.repository.FavoriteRepository
import com.locationjoystick.core.data.repository.LocationRepository
import com.locationjoystick.core.model.FavoriteLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val locationRepository: LocationRepository,
) : ViewModel() {

    private val _deletedFavorite = MutableStateFlow<FavoriteLocation?>(null)

    val uiState: StateFlow<FavoritesUiState> = favoriteRepository.getAllFavorites()
        .map { favorites ->
            FavoritesUiState(
                favorites = favorites,
                isLoading = false,
                deletedFavorite = _deletedFavorite.value
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FavoritesUiState(isLoading = true)
        )

    fun teleportTo(favorite: FavoriteLocation) {
        viewModelScope.launch {
            locationRepository.updatePosition(favorite.lat, favorite.lon)
        }
    }

    fun deleteFavorite(favoriteId: String) {
        viewModelScope.launch {
            favoriteRepository.deleteFavorite(favoriteId)
        }
    }

    fun renameFavorite(favoriteId: String, newName: String) {
        viewModelScope.launch {
            favoriteRepository.renameFavorite(favoriteId, newName)
        }
    }
}
