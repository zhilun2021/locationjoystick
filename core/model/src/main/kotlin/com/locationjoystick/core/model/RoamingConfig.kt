package com.locationjoystick.core.model

data class RoamingConfig(
    val centerPosition: LatLng,
    val radiusMeters: Double,
    val durationSeconds: Long,
    val useRoadSnapping: Boolean = false,
)
