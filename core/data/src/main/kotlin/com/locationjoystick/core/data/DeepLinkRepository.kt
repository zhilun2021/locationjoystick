package com.locationjoystick.core.data

import com.locationjoystick.core.model.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeepLinkRepository
    @Inject
    constructor() {
        private val _pendingCoords = MutableSharedFlow<LatLng>(replay = 0)
        val pendingCoords: Flow<LatLng> = _pendingCoords.asSharedFlow()

        fun setPendingCoords(
            lat: Double,
            lon: Double,
        ) {
            _pendingCoords.tryEmit(LatLng(lat, lon))
        }
    }
