package com.locationjoystick.core.model

data class FavoriteLocation(
    val id: String,
    val name: String,
    val position: LatLng,
    val createdAt: Long = 0L,
)
