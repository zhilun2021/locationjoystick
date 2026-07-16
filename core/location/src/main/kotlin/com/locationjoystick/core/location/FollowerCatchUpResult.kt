package com.locationjoystick.core.location

import com.locationjoystick.core.common.constants.AppConstants
import com.locationjoystick.core.common.util.advancePosition
import com.locationjoystick.core.common.util.calculateBearing
import com.locationjoystick.core.common.util.haversineDistance
import com.locationjoystick.core.model.LatLng

/**
 * Result of one [computeFollowerCatchUp] step.
 *
 * @property bearing New bearing to report, or null to leave the current bearing untouched —
 *   matches [buildLocation]'s bearing-hold semantics: a stationary follower (arrived, or the
 *   step overshot the target) should hold its last heading rather than snap to 0.
 */
internal data class FollowerCatchUpResult(
    val latitude: Double,
    val longitude: Double,
    val speedMs: Float,
    val bearing: Float?,
)

/**
 * Pure step function for [MockLocationService.advanceFollowerCatchUp]: walks [current] toward
 * [target] at [activeProfileSpeedMs], instead of snapping straight to it. Snaps (and zeroes
 * speed) once within [AppConstants.LocationConstants.WALK_ARRIVAL_THRESHOLD_METERS] or when a
 * single tick's step would overshoot the target.
 */
internal fun computeFollowerCatchUp(
    current: LatLng,
    target: LatLng,
    activeProfileSpeedMs: Double,
): FollowerCatchUpResult {
    val distanceM = haversineDistance(current, target)
    if (distanceM <= AppConstants.LocationConstants.WALK_ARRIVAL_THRESHOLD_METERS) {
        return FollowerCatchUpResult(target.latitude, target.longitude, 0f, null)
    }
    val bearing = calculateBearing(current.latitude, current.longitude, target.latitude, target.longitude)
    val stepM = activeProfileSpeedMs * (AppConstants.LocationConstants.UPDATE_INTERVAL_MS / 1000.0)
    if (stepM >= distanceM) {
        return FollowerCatchUpResult(target.latitude, target.longitude, 0f, null)
    }
    val (newLat, newLon) = advancePosition(current.latitude, current.longitude, bearing, stepM)
    return FollowerCatchUpResult(newLat, newLon, activeProfileSpeedMs.toFloat(), bearing.toFloat())
}
