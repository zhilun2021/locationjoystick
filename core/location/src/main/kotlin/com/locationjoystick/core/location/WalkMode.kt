package com.locationjoystick.core.location

import com.locationjoystick.core.model.LatLng

/** Represents the current walk/ephemeral-replay state across all map surfaces. */
sealed interface WalkMode {
    data object Idle : WalkMode

    /** Walking toward a single target via WalkCoordinator. */
    data class Walking(
        val target: LatLng,
        val start: LatLng?,
        val isViaRoads: Boolean = false,
    ) : WalkMode

    /** Replaying a chain of user-tapped waypoints (ephemeral route) via RouteReplayEngine. */
    data class EphemeralReplay(
        val waypoints: List<LatLng>,
        val followRoads: Boolean = false,
    ) : WalkMode
}
