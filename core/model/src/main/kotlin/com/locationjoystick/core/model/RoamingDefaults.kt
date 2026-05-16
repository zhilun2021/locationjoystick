package com.locationjoystick.core.model

data class RoamingDefaults(
    val radiusMeters: Double = 5_000.0,
    val distanceMeters: Double = 1_000.0,
    val speedProfileId: String = "walk",
    val followRoads: Boolean = true,
    val returnToInitialLocation: Boolean = true,
)
