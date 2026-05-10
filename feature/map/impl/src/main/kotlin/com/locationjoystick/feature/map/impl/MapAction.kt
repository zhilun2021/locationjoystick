package com.locationjoystick.feature.map.impl

import com.locationjoystick.core.model.LatLng

sealed interface MapAction {
    data class TapToTeleport(val position: LatLng) : MapAction
    data class LongPressTapToWalk(val position: LatLng) : MapAction
    data object StartSpoofing : MapAction
    data object StopSpoofing : MapAction
    data object RecenterCamera : MapAction
    data object UserStartedPanning : MapAction
}
