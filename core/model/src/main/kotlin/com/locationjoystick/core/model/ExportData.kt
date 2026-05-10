package com.locationjoystick.core.model

data class ExportData(
    val schemaVersion: Int = 1,
    val exportedAt: Long = 0L,
    val settings: AppSettings = AppSettings(),
    val speedProfiles: List<SpeedProfile> = SpeedProfile.defaultProfiles(),
    val routes: List<Route> = emptyList(),
    val favoriteLocations: List<FavoriteLocation> = emptyList(),
)
