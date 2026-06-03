package com.locationjoystick.core.location

import android.content.Context
import com.locationjoystick.core.data.LocationRepository
import com.locationjoystick.core.data.RouteRepository
import com.locationjoystick.core.data.SettingsRepository
import com.locationjoystick.core.data.TeleportUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StartRouteReplayUseCase
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val settingsRepository: SettingsRepository,
        private val locationRepository: LocationRepository,
        private val routeRepository: RouteRepository,
        private val teleportUseCase: TeleportUseCase,
    ) {
        suspend fun execute(
            routeId: String,
            isLooping: Boolean = false,
            isReverse: Boolean = false,
            isReturnToLocation: Boolean = false,
            teleportToStart: Boolean = false,
        ) {
            val speedMs = settingsRepository.getActiveSpeedProfile().first().speedMetersPerSecond
            val returnPosition = if (isReturnToLocation) locationRepository.currentPosition.value else null
            if (teleportToStart) {
                val waypoints =
                    routeRepository
                        .getRoutes()
                        .first()
                        .find { it.id == routeId }
                        ?.waypoints
                if (!waypoints.isNullOrEmpty()) {
                    val startWaypoint = if (isReverse) waypoints.last() else waypoints.first()
                    teleportUseCase.execute(startWaypoint.position)
                }
            }
            val intent =
                MockLocationIntentBuilder
                    .startRouteReplay(context, routeId, speedMs, isReverse)
                    .apply {
                        putExtra(MockLocationService.EXTRA_IS_LOOPING, isLooping)
                        if (returnPosition != null) {
                            putExtra(MockLocationService.EXTRA_RETURN_LAT, returnPosition.latitude)
                            putExtra(MockLocationService.EXTRA_RETURN_LON, returnPosition.longitude)
                        }
                    }
            context.startService(intent)
        }
    }
