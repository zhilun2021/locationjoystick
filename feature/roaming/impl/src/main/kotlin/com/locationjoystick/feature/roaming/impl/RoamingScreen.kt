package com.locationjoystick.feature.roaming.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
internal fun RoamingRoute(
    viewModel: RoamingViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RoamingScreen(
        uiState = uiState,
        onUpdateRadius = viewModel::updateRadius,
        onUpdateDuration = viewModel::updateDuration,
        onToggleOsrm = viewModel::toggleOsrmRouting,
        onStartRoaming = viewModel::startRoaming,
        onStopRoaming = viewModel::stopRoaming
    )
}

@Composable
internal fun RoamingScreen(
    uiState: RoamingUiState,
    onUpdateRadius: (Double) -> Unit,
    onUpdateDuration: (Int) -> Unit,
    onToggleOsrm: (Boolean) -> Unit,
    onStartRoaming: () -> Unit,
    onStopRoaming: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Radius: ${(uiState.config.radiusMeters / 1000).toInt()} km")
            Slider(
                value = uiState.config.radiusMeters.toFloat(),
                onValueChange = { onUpdateRadius(it.toDouble()) },
                valueRange = 100f..50000f
            )

            Text("Duration: ${uiState.config.durationMinutes} min")
            Slider(
                value = uiState.config.durationMinutes.toFloat(),
                onValueChange = { onUpdateDuration(it.toInt()) },
                valueRange = 1f..240f
            )

            Switch(
                checked = uiState.config.useOsrmRouting,
                onCheckedChange = onToggleOsrm,
                label = { Text("Follow roads (OSRM)") }
            )

            if (uiState.isRoaming) {
                Text("Roaming: ${uiState.elapsedMinutes.toInt()} / ${uiState.config.durationMinutes} min")
                Button(onClick = onStopRoaming) {
                    Text("Stop")
                }
            } else {
                Button(
                    onClick = onStartRoaming,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Start Roaming")
                }
            }
        }
    }
}
