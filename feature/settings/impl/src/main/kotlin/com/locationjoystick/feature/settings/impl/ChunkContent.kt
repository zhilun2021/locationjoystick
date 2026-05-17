package com.locationjoystick.feature.settings.impl

import com.locationjoystick.core.common.constants.AppConstants
import com.locationjoystick.core.model.AppSettings
import com.locationjoystick.core.model.ExportData
import com.locationjoystick.core.model.FavoriteLocation
import com.locationjoystick.core.model.Route
import com.locationjoystick.core.model.SpeedProfile

sealed class ChunkContent {
    data class Settings(
        val payload: AppSettings,
    ) : ChunkContent()

    data class Routes(
        val payload: List<Route>,
    ) : ChunkContent()

    data class Favorites(
        val payload: List<FavoriteLocation>,
    ) : ChunkContent()
}

internal fun mergeChunkContents(allContent: List<ChunkContent>): ExportData {
    val settings = allContent.filterIsInstance<ChunkContent.Settings>().firstOrNull()?.payload
    val routes = allContent.filterIsInstance<ChunkContent.Routes>().flatMap { it.payload }
    val favorites = allContent.filterIsInstance<ChunkContent.Favorites>().flatMap { it.payload }
    return ExportData(
        schemaVersion = AppConstants.ExportConstants.SCHEMA_VERSION,
        exportedAt = System.currentTimeMillis(),
        settings = settings ?: AppSettings(),
        speedProfiles = SpeedProfile.defaultProfiles(),
        routes = routes,
        favoriteLocations = favorites,
    )
}
