package com.locationjoystick.feature.settings.impl

import com.locationjoystick.core.model.AppSettings

data class SettingsUiState(
    val settings: AppSettings? = null,
    val isLoading: Boolean = true
)
