package com.locationjoystick.feature.roaming.impl

import com.locationjoystick.core.model.LatLng
import com.locationjoystick.core.model.RoamingConfig

data class RoamingUiState(
    val config: RoamingConfig = RoamingConfig(
        centerPosition = LatLng(0.0, 0.0),
        radiusMeters = 2000.0,
        durationMinutes = 30,
        speedProfileId = "walk",
        useOsrmRouting = false
    ),
    val isRoaming: Boolean = false,
    val elapsedMinutes: Float = 0f,
    val currentPosition: LatLng? = null,
    val osrmAvailable: Boolean = true
)
