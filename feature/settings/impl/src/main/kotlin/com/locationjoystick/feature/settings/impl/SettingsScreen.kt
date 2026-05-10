package com.locationjoystick.feature.settings.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
internal fun SettingsRoute(
    viewModel: SettingsViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(
        uiState = uiState,
        onResetToDefaults = viewModel::resetToDefaults
    )
}

@Composable
internal fun SettingsScreen(
    uiState: SettingsUiState,
    onResetToDefaults: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            uiState.settings != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text("Speed Profiles")
                    Text("Joystick Settings")
                    Text("Widget Configuration")
                    Text("GPS Simulation")
                    Text("Data Management")

                    Button(
                        onClick = onResetToDefaults,
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Reset to Defaults")
                    }
                }
            }
        }
    }
}
