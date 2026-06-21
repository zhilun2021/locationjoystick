package com.locationjoystick.core.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Thin Hilt ViewModel wrapping [MapController.isSpoofing] / [MapController.toggleSpoofing] so any
 * screen's top bar can drive the global start/stop spoofing state without each feature's own
 * ViewModel needing to depend on [MapController] directly.
 */
@HiltViewModel
class SpoofToggleViewModel
    @Inject
    constructor(
        private val mapController: MapController,
    ) : ViewModel() {
        val isSpoofing: StateFlow<Boolean> = mapController.isSpoofing

        fun toggle() {
            mapController.toggleSpoofing()
        }
    }

data class SpoofToggleState(
    val isSpoofing: Boolean,
    val onToggle: () -> Unit,
)

/**
 * Collects the global start/stop spoofing state from [SpoofToggleViewModel] so every screen's
 * `LjScaffold` call site doesn't need to repeat `hiltViewModel()` + `collectAsStateWithLifecycle()`.
 */
@Composable
fun rememberSpoofToggleState(): SpoofToggleState {
    val viewModel: SpoofToggleViewModel = hiltViewModel()
    val isSpoofing by viewModel.isSpoofing.collectAsStateWithLifecycle()
    return SpoofToggleState(isSpoofing, viewModel::toggle)
}
