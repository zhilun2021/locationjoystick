package com.locationjoystick.core.data

import com.locationjoystick.core.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeepLinkRepository @Inject constructor() {
    private val _pendingCoords = MutableStateFlow<LatLng?>(null)
    val pendingCoords: StateFlow<LatLng?> = _pendingCoords.asStateFlow()

    fun setPendingCoords(lat: Double, lon: Double) {
        _pendingCoords.value = LatLng(lat, lon)
    }

    fun consume() {
        _pendingCoords.value = null
    }
}
