package com.locationjoystick.feature.map.impl

import com.locationjoystick.core.model.LatLng
import com.locationjoystick.core.model.MockLocationState
import com.locationjoystick.core.model.Route

data class MapUiState(
    val currentPosition: LatLng? = null,
    val mockLocationState: MockLocationState = MockLocationState.IDLE,
    val routes: List<Route> = emptyList(),
    val isUserPanning: Boolean = false,
)

val MapUiState.isSpoofing: Boolean
    get() = mockLocationState == MockLocationState.RUNNING
