package com.locationjoystick.core.routing

import android.content.Context
import android.util.Log
import com.locationjoystick.core.data.LocationRepository
import com.locationjoystick.core.location.MockLocationIntentBuilder
import com.locationjoystick.core.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "WalkCoordinator"

/**
 * Thin facade over [WalkToEngine] for use inside a ViewModel or Service.
 *
 * Handles:
 * - Cancelling any in-flight walk before starting a new one.
 * - Forwarding position ticks to [MockLocationService] via [MockLocationIntentBuilder].
 * - Clearing [LocationRepository.walkTarget] on arrival / cancellation.
 *
 * The caller provides a [CoroutineScope] (typically `viewModelScope` or a
 * service scope) and never manages the [Job] directly.
 *
 * Usage:
 * ```kotlin
 * walkCoordinator.startWalk(target, viewModelScope)
 * walkCoordinator.cancel()
 * ```
 */
@Singleton
class WalkCoordinator
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val locationRepository: LocationRepository,
        private val walkToEngine: WalkToEngine,
    ) {
        private var activeWalkJob: Job? = null

        /**
         * Starts a walk toward [target] on the given [scope].
         *
         * Any previously active walk is cancelled first.
         */
        fun startWalk(
            target: LatLng,
            scope: CoroutineScope,
        ) {
            activeWalkJob?.cancel()
            locationRepository.setWalkTarget(target)

            with(walkToEngine) {
                activeWalkJob =
                    scope.launchWalkTo(
                        target = target,
                        onPositionUpdate = { newPos ->
                            locationRepository.updatePosition(newPos)
                            context.startService(
                                MockLocationIntentBuilder.updatePosition(
                                    context,
                                    newPos.latitude,
                                    newPos.longitude,
                                ),
                            )
                        },
                        onArrival = {
                            Log.d(TAG, "Arrived at $target")
                            locationRepository.setWalkTarget(null)
                        },
                    )
            }
        }

        /** Cancels any active walk and clears the walk target. */
        fun cancel() {
            activeWalkJob?.cancel()
            activeWalkJob = null
            locationRepository.setWalkTarget(null)
        }
    }
