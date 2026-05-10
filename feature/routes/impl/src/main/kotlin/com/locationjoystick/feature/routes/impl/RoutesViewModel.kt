package com.locationjoystick.feature.routes.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.locationjoystick.core.data.repository.RouteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutesViewModel @Inject constructor(
    private val routeRepository: RouteRepository,
) : ViewModel() {

    val uiState: StateFlow<RoutesUiState> = routeRepository.getAllRoutes()
        .map { routes -> RoutesUiState(routes = routes, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RoutesUiState(isLoading = true)
        )

    fun deleteRoute(routeId: String) {
        viewModelScope.launch {
            routeRepository.deleteRoute(routeId)
        }
    }
}
