package com.locationjoystick.feature.routes.impl

data class RoutePlaybackState(
    val activeRouteId: String? = null,
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val isBackward: Boolean = false,
)
