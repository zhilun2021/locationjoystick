package com.locationjoystick.core.routing

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Application-scoped channel for user-visible routing failures. Replaces the previous
 * per-controller `_routingErrors` flow on `MapController` so other routing-adjacent classes
 * (e.g. [EphemeralReplayController][com.locationjoystick.core.location.EphemeralReplayController],
 * [RoamingEngine]) can report without depending on `MapController`.
 */
@Singleton
class RoutingErrorReporter
    @Inject
    constructor() {
        private val _errors = MutableSharedFlow<String>(extraBufferCapacity = 1)
        val errors: SharedFlow<String> = _errors.asSharedFlow()

        fun report(message: String) {
            _errors.tryEmit(message)
        }
    }
