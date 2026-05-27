package com.locationjoystick.core.model

data class RoamingDefaults(
    val radiusMeters: Double = 5_000.0,
    val distanceMeters: Double = 1_000.0,
    val speedProfileId: String = "walk",
    val followRoads: Boolean = true,
    val returnToInitialLocation: Boolean = true,
)

/**
 * Converts defaults into a [RoamingConfig] for a specific [centerPosition].
 * This is the single translation point for the followRoads → useRoadSnapping rename.
 */
fun RoamingDefaults.toConfig(centerPosition: LatLng): RoamingConfig =
    RoamingConfig(
        centerPosition = centerPosition,
        radiusMeters = radiusMeters,
        distanceMeters = distanceMeters,
        speedProfileId = speedProfileId,
        useRoadSnapping = followRoads,
        returnToInitialLocation = returnToInitialLocation,
    )
