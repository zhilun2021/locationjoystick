package com.locationjoystick.feature.settings.impl

import com.locationjoystick.core.common.constants.AppConstants
import com.locationjoystick.core.model.AppSettings
import com.locationjoystick.core.model.ExportData
import com.locationjoystick.core.model.FavoriteLocation
import com.locationjoystick.core.model.Route
import com.locationjoystick.core.model.SpeedProfile
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.util.Base64
import java.util.zip.GZIPInputStream

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

internal fun decodeChunkEnvelope(envelope: ChunkEnvelope): List<ChunkContent> {
    val decoded = Base64.getDecoder().decode(envelope.d)
    val decompressed = GZIPInputStream(ByteArrayInputStream(decoded)).readBytes()
    val json = String(decompressed, Charsets.UTF_8)
    return parseChunkContentJson(json)
}

internal fun parseChunkContentJson(json: String): List<ChunkContent> {
    val array = JSONArray(json)
    return (0 until array.length()).map { idx ->
        val obj = array.getJSONObject(idx)
        when (val type = obj.getString("type")) {
            "settings" -> {
                val wrapped =
                    JSONObject().apply {
                        put("settings", obj.getJSONObject("payload"))
                        put("schemaVersion", AppConstants.ExportConstants.SCHEMA_VERSION)
                    }
                ChunkContent.Settings(SettingsExportCodec.parseExportData(wrapped.toString()).settings)
            }

            "routes" -> {
                val wrapped =
                    JSONObject().apply {
                        put("routes", obj.getJSONArray("payload"))
                        put("schemaVersion", AppConstants.ExportConstants.SCHEMA_VERSION)
                    }
                ChunkContent.Routes(SettingsExportCodec.parseExportData(wrapped.toString()).routes)
            }

            "favorites" -> {
                val wrapped =
                    JSONObject().apply {
                        put("favoriteLocations", obj.getJSONArray("payload"))
                        put("schemaVersion", AppConstants.ExportConstants.SCHEMA_VERSION)
                    }
                ChunkContent.Favorites(SettingsExportCodec.parseExportData(wrapped.toString()).favoriteLocations)
            }

            else -> {
                error("Unknown chunk type: $type")
            }
        }
    }
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
