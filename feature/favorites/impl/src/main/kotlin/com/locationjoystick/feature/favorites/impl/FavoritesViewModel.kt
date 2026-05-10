package com.locationjoystick.feature.favorites.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.locationjoystick.core.data.FavoriteRepository
import com.locationjoystick.core.data.LocationRepository
import com.locationjoystick.core.model.FavoriteLocation
import dagger.hilt.android.lifecycle.HiltViewModel
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

    val uiState: StateFlow<FavoritesUiState> = favoriteRepository.getFavorites()
        .map { favorites ->
            FavoritesUiState(
                favorites = favorites,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FavoritesUiState(isLoading = true)
        )

    fun teleportTo(favorite: FavoriteLocation) {
        viewModelScope.launch {
            locationRepository.updatePosition(favorite.position)
        }
    }

    fun deleteFavorite(favoriteId: String) {
        viewModelScope.launch {
            favoriteRepository.deleteFavorite(favoriteId)
        }
    }

    fun renameFavorite(favoriteId: String, newName: String) {
        viewModelScope.launch {
            val favorite = uiState.value.favorites.find { it.id == favoriteId } ?: return@launch
            favoriteRepository.updateFavorite(favorite.copy(name = newName))
        }
    }
}
