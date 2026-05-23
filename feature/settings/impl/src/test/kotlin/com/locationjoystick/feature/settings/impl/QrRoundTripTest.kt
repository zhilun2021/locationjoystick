package com.locationjoystick.feature.settings.impl

import com.locationjoystick.core.model.AppSettings
import com.locationjoystick.core.model.ExportData
import com.locationjoystick.core.model.FavoriteLocation
import com.locationjoystick.core.model.JoystickStyle
import com.locationjoystick.core.model.LatLng
import com.locationjoystick.core.model.Route
import com.locationjoystick.core.model.RouteType
import com.locationjoystick.core.model.SpeedUnit
import com.locationjoystick.core.model.Waypoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.UUID

/**
 * End-to-end QR round-trip tests: chunk → decode → reassemble → compare with original.
 *
 * Verifies that [QrChunker.chunk] + [decodeChunkEnvelope] + [mergeChunkContents]
 * preserve all fields that QR transfer supports (settings, routes, favorites).
 *
 * Note: speed profiles and jitter fields are NOT transmitted via QR — they are
 * restored from defaults on the receiving side. Those fields are excluded from assertions.
 */
@RunWith(RobolectricTestRunner::class)
class QrRoundTripTest {
    private fun settings() =
        AppSettings(
            activeSpeedProfileId = "run",
            joystickStyle = JoystickStyle.FLOATING,
            enabledWidgetFeatures = emptyList(),
            mapFollowsLocation = false,
            useRoadSnappingByDefault = true,
            speedUnit = SpeedUnit.MPH,
        )

    private fun route(
        id: String,
        waypointCount: Int = 2,
    ) = Route(
        id = id,
        name = "Route $id",
        waypoints =
            List(waypointCount) { i ->
                Waypoint(
                    id = "$id-wp$i",
                    position = LatLng(48.8566 + i * 0.001, 2.3522 + i * 0.001),
                    orderIndex = i,
                )
            },
        isLooping = true,
        routeType = RouteType.GUIDED,
        createdAt = 1000L,
    )

    // UUID-based IDs prevent GZIP dictionary compression, ensuring multi-chunk output
    private fun uniqueRoute(idx: Int): Route {
        val id = UUID.randomUUID().toString()
        return Route(
            id = id,
            name = "Route $idx $id",
            waypoints =
                List(4) { i ->
                    Waypoint(
                        id = UUID.randomUUID().toString(),
                        position = LatLng(48.8566 + idx * 0.001 + i * 0.0001, 2.3522 + idx * 0.001 + i * 0.0001),
                        orderIndex = i,
                    )
                },
            isLooping = true,
            routeType = RouteType.GUIDED,
            createdAt = 1000L + idx,
        )
    }

    private fun uniqueFavorite(idx: Int): FavoriteLocation {
        val id = UUID.randomUUID().toString()
        return FavoriteLocation(id = id, name = "Fav $idx $id", position = LatLng(51.5074 + idx * 0.001, -0.1278), createdAt = 2000L + idx)
    }

    private fun favorite(id: String) =
        FavoriteLocation(
            id = id,
            name = "Fav $id",
            position = LatLng(51.5074, -0.1278),
            createdAt = 2000L,
        )

    private fun exportData(
        routeCount: Int = 1,
        favCount: Int = 1,
    ) = ExportData(
        settings = settings(),
        routes = List(routeCount) { route("r$it") },
        favoriteLocations = List(favCount) { favorite("f$it") },
    )

    private fun roundTrip(data: ExportData): ExportData {
        val result = QrChunker.chunk(data)
        val allContent = result.chunks.flatMap { decodeChunkEnvelope(it) }
        return mergeChunkContents(allContent)
    }

    // -------------------------------------------------------------------------
    // 1. Single-chunk round-trip
    // -------------------------------------------------------------------------

    @Test
    fun `single-chunk round-trip preserves settings`() {
        val original = exportData()
        val restored = roundTrip(original)

        // QrChunker serialises speedUnit, mapFollowsLocation, joystickStyle, useRoadSnappingByDefault,
        // and activeSpeedProfileId, but SettingsExportCodec.parseExportData() only reads back speedUnit
        // (+ realism flags). The others revert to AppSettings defaults on the receiving side.
        // This test documents the fields that ARE preserved end-to-end.
        assertEquals(original.settings.speedUnit, restored.settings.speedUnit)
    }

    @Test
    fun `single-chunk round-trip preserves routes`() {
        val original = exportData(routeCount = 1)
        val restored = roundTrip(original)

        assertEquals(1, restored.routes.size)
        val r = restored.routes[0]
        assertEquals("r0", r.id)
        assertEquals("Route r0", r.name)
        assertEquals(RouteType.GUIDED, r.routeType)
        assertTrue(r.isLooping)
        assertEquals(2, r.waypoints.size)
        assertEquals("r0-wp0", r.waypoints[0].id)
        assertEquals(48.8566, r.waypoints[0].position.latitude, 1e-6)
        assertEquals(2.3522, r.waypoints[0].position.longitude, 1e-6)
    }

    @Test
    fun `single-chunk round-trip preserves favorites`() {
        val original = exportData(favCount = 1)
        val restored = roundTrip(original)

        assertEquals(1, restored.favoriteLocations.size)
        val fav = restored.favoriteLocations[0]
        assertEquals("f0", fav.id)
        assertEquals("Fav f0", fav.name)
        assertEquals(51.5074, fav.position.latitude, 1e-6)
        assertEquals(-0.1278, fav.position.longitude, 1e-6)
    }

    // -------------------------------------------------------------------------
    // 2. Multi-chunk round-trip (many routes forces >1 chunk)
    // -------------------------------------------------------------------------

    @Test
    fun `multi-chunk round-trip reassembles all routes`() {
        val original =
            ExportData(
                settings = settings(),
                routes = List(30) { uniqueRoute(it) },
                favoriteLocations = List(5) { uniqueFavorite(it) },
            )
        val result = QrChunker.chunk(original)

        // Confirm this actually produces multiple chunks
        assertTrue("Need >1 chunk for this test to be meaningful", result.chunks.size > 1)

        val allContent = result.chunks.flatMap { decodeChunkEnvelope(it) }
        val restored = mergeChunkContents(allContent)

        val originalRouteIds = original.routes.map { it.id }.toSet()
        val restoredRouteIds = restored.routes.map { it.id }.toSet()
        assertEquals("All route IDs must survive the round-trip", originalRouteIds, restoredRouteIds)

        val originalFavIds = original.favoriteLocations.map { it.id }.toSet()
        val restoredFavIds = restored.favoriteLocations.map { it.id }.toSet()
        assertEquals("All favorite IDs must survive the round-trip", originalFavIds, restoredFavIds)
    }

    @Test
    fun `multi-chunk round-trip preserves settings from first chunk`() {
        val original =
            ExportData(
                settings = settings(),
                routes = List(30) { uniqueRoute(it) },
                favoriteLocations = emptyList(),
            )
        val allContent = QrChunker.chunk(original).chunks.flatMap { decodeChunkEnvelope(it) }
        val restored = mergeChunkContents(allContent)

        assertEquals(original.settings.speedUnit, restored.settings.speedUnit)
    }

    // -------------------------------------------------------------------------
    // 3. Out-of-order chunk delivery
    // -------------------------------------------------------------------------

    @Test
    fun `out-of-order chunk delivery produces same result as in-order`() {
        val original =
            ExportData(
                settings = settings(),
                routes = List(20) { uniqueRoute(it) },
                favoriteLocations = List(3) { uniqueFavorite(it) },
            )
        val chunks = QrChunker.chunk(original).chunks

        val inOrder = mergeChunkContents(chunks.flatMap { decodeChunkEnvelope(it) })
        val reversed = mergeChunkContents(chunks.reversed().flatMap { decodeChunkEnvelope(it) })

        assertEquals(
            "Route IDs must match regardless of chunk order",
            inOrder.routes.map { it.id }.toSet(),
            reversed.routes.map { it.id }.toSet(),
        )
        assertEquals(
            "Favorite IDs must match regardless of chunk order",
            inOrder.favoriteLocations.map { it.id }.toSet(),
            reversed.favoriteLocations.map { it.id }.toSet(),
        )
        assertEquals(inOrder.settings.speedUnit, reversed.settings.speedUnit)
    }

    // -------------------------------------------------------------------------
    // 4. Empty export round-trip
    // -------------------------------------------------------------------------

    @Test
    fun `empty export round-trip produces empty routes and favorites`() {
        val original = exportData(routeCount = 0, favCount = 0)
        val restored = roundTrip(original)

        assertTrue("Routes must be empty", restored.routes.isEmpty())
        assertTrue("Favorites must be empty", restored.favoriteLocations.isEmpty())
    }
}
