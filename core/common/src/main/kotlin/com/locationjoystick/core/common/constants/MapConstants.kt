package com.locationjoystick.core.common.constants

/**
 * Shared map constants used across feature modules.
 */
object MapConstants {
    // Default map center (Paris)
    const val DEFAULT_LAT = 48.8566
    const val DEFAULT_LON = 2.3522

    // Default zoom level for all map screens
    const val DEFAULT_ZOOM = 30.0

    // OSM tile source URL
    const val OSM_TILE_URL = "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
}
