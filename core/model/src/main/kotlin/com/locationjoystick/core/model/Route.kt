package com.locationjoystick.core.model

data class Route(
    val id: String,
    val name: String,
    val waypoints: List<Waypoint> = emptyList(),
    val isLooping: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)
