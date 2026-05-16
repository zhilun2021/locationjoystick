package com.locationjoystick.core.model

data class RoamingConfig(
    val centerPosition: LatLng,
    val radiusMeters: Double,
    val distanceMeters: Double,
    val speedProfileId: String = "walk",
    val useRoadSnapping: Boolean = false,
    val returnToInitialLocation: Boolean = true,
)
