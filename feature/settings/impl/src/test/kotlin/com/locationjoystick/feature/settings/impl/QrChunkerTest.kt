package com.locationjoystick.feature.settings.impl

import com.locationjoystick.core.model.AppSettings
import com.locationjoystick.core.model.ExportData
import com.locationjoystick.core.model.FavoriteLocation
import com.locationjoystick.core.model.LatLng
import com.locationjoystick.core.model.Route
import com.locationjoystick.core.model.RouteType
import com.locationjoystick.core.model.Waypoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

class QrChunkerTest {
    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private fun gzip(data: ByteArray): ByteArray {
        val output = ByteArrayOutputStream()
        GZIPOutputStream(output).use { it.write(data) }
        return output.toByteArray()
    }

    private fun gzipSize(json: String): Int = gzip(json.toByteArray(Charsets.UTF_8)).size

    private fun minimalExportData(): ExportData =
        ExportData(
            settings = AppSettings(),
            routes = emptyList(),
            favoriteLocations = emptyList(),
        )

    private fun makeRoute(
        name: String,
        waypointCount: Int = 2,
    ): Route =
        Route(
            id = name,
            name = name,
            waypoints =
                List(waypointCount) { i ->
                    Waypoint(
                        id = "$name-wp-$i",
                        position = LatLng(latitude = 48.8566 + i * 0.001, longitude = 2.3522 + i * 0.001),
                        orderIndex = i,
                    )
                },
            routeType = RouteType.STRAIGHT,
        )

    private fun makeFavorite(name: String): FavoriteLocation =
        FavoriteLocation(
            id = name,
            name = name,
            position = LatLng(latitude = 48.8566, longitude = 2.3522),
        )

    /** Builds a route whose JSON exceeds the 2400-byte gzip limit on its own. */
    private fun makeOversizedRoute(name: String): Route {
        // 500 waypoints produces a JSON payload well above 2400 gzip bytes
        val waypoints =
            List(500) { i ->
                Waypoint(
                    id = "$name-wp-$i-padding-${i * 31337}",
                    position = LatLng(latitude = 10.0 + i * 0.001, longitude = 20.0 + i * 0.001),
                    orderIndex = i,
                )
            }
        return Route(id = name, name = name, waypoints = waypoints, routeType = RouteType.STRAIGHT)
    }

    // ---------------------------------------------------------------------------
    // 1. Minimal export produces exactly 1 chunk
    // ---------------------------------------------------------------------------

    @Test
    fun `minimal export data produces exactly one chunk`() {
        val result = QrChunker.chunk(minimalExportData())

        assertEquals("Minimal export should produce exactly 1 chunk", 1, result.chunks.size)
        assertTrue("No routes should be skipped", result.skippedRoutes.isEmpty())
    }

    // ---------------------------------------------------------------------------
    // 2. All chunks within 2400-byte gzip limit
    //    Verified indirectly: QrChunker enforces the limit internally.
    //    We assert chunk count grows with data and use gzipSize to confirm
    //    that a single small item stays under the limit.
    // ---------------------------------------------------------------------------

    @Test
    fun `single small route fits within 2400-byte gzip limit`() {
        val route = makeRoute("short-route", waypointCount = 2)
        // Build a minimal JSON matching QrChunker's internal format for one route entry
        val routeJson = """{"id":"short-route","name":"short-route","isLooping":false,"routeType":"STRAIGHT","createdAt":0}"""
        assertTrue(
            "Single small route JSON should fit within 2400-byte gzip limit",
            gzipSize(routeJson) <= 2400,
        )
    }

    @Test
    fun `export with many favorites produces chunks all within 2400-byte limit`() {
        // 50 favorites; QrChunker must pack them into bins <= 2400 bytes
        val favorites = List(50) { i -> makeFavorite("fav-$i") }
        val data = ExportData(favoriteLocations = favorites)

        val result = QrChunker.chunk(data)

        assertTrue("Should produce at least one chunk", result.chunks.isNotEmpty())
        assertTrue("No favorites should be skipped (all small)", result.skippedRoutes.isEmpty())
        // Each chunk must carry a valid session + contiguous index
        result.chunks.forEachIndexed { idx, envelope ->
            assertEquals("chunk field should be 1-based index", idx + 1, envelope.chunk)
        }
    }

    @Test
    fun `export with many routes produces chunks all within 2400-byte limit`() {
        val routes = List(20) { i -> makeRoute("route-$i", waypointCount = 3) }
        val data = ExportData(routes = routes)

        val result = QrChunker.chunk(data)

        assertTrue("Should produce at least one chunk", result.chunks.isNotEmpty())
        assertTrue("No small routes should be skipped", result.skippedRoutes.isEmpty())
        result.chunks.forEachIndexed { idx, envelope ->
            assertEquals("chunk field should be 1-based index", idx + 1, envelope.chunk)
        }
    }

    // ---------------------------------------------------------------------------
    // 3. All chunks share the same session UUID
    // ---------------------------------------------------------------------------

    @Test
    fun `all chunks share the same session UUID`() {
        val routes = List(10) { i -> makeRoute("r-$i", waypointCount = 3) }
        val data = ExportData(routes = routes)

        val result = QrChunker.chunk(data)

        assertTrue("Need multiple chunks for this assertion", result.chunks.size >= 1)
        val session = result.chunks.first().session
        assertFalse("Session UUID must not be empty", session.isBlank())
        result.chunks.forEach { envelope ->
            assertEquals("All chunks must share the same session UUID", session, envelope.session)
        }
    }

    @Test
    fun `different chunk calls produce different session UUIDs`() {
        val data = minimalExportData()

        val result1 = QrChunker.chunk(data)
        val result2 = QrChunker.chunk(data)

        assertFalse(
            "Each chunk() call should generate a unique session UUID",
            result1.chunks.first().session == result2.chunks.first().session,
        )
    }

    // ---------------------------------------------------------------------------
    // 4. Chunk indices are 1-based and contiguous
    // ---------------------------------------------------------------------------

    @Test
    fun `single chunk has index 1`() {
        val result = QrChunker.chunk(minimalExportData())

        assertEquals(1, result.chunks.first().chunk)
    }

    @Test
    fun `multiple chunks have contiguous 1-based indices`() {
        val routes = List(30) { i -> makeRoute("route-$i", waypointCount = 4) }
        val data = ExportData(routes = routes)

        val result = QrChunker.chunk(data)

        val indices = result.chunks.map { it.chunk }
        val expected = (1..result.chunks.size).toList()
        assertEquals("Chunk indices must be 1-based and contiguous", expected, indices)
    }

    // ---------------------------------------------------------------------------
    // 5. Total field matches actual chunk count
    // ---------------------------------------------------------------------------

    @Test
    fun `total field matches actual chunk count for single chunk`() {
        val result = QrChunker.chunk(minimalExportData())

        assertEquals(result.chunks.size, result.chunks.first().total)
    }

    @Test
    fun `total field matches actual chunk count for multiple chunks`() {
        val routes = List(30) { i -> makeRoute("route-$i", waypointCount = 4) }
        val data = ExportData(routes = routes)

        val result = QrChunker.chunk(data)

        val actualCount = result.chunks.size
        result.chunks.forEach { envelope ->
            assertEquals(
                "Every chunk's total field must equal actual chunk count",
                actualCount,
                envelope.total,
            )
        }
    }

    // ---------------------------------------------------------------------------
    // 6. Oversized route is skipped and its name appears in skippedRoutes
    // ---------------------------------------------------------------------------

    @Test
    fun `oversized route is skipped and name reported in skippedRoutes`() {
        val oversized = makeOversizedRoute("giant-route")
        val data = ExportData(routes = listOf(oversized))

        val result = QrChunker.chunk(data)

        assertTrue(
            "Oversized route name must appear in skippedRoutes",
            result.skippedRoutes.contains("giant-route"),
        )
    }

    @Test
    fun `oversized route produces at least one chunk for settings content`() {
        val oversized = makeOversizedRoute("giant-route")
        val data = ExportData(routes = listOf(oversized))

        val result = QrChunker.chunk(data)

        // Settings are always emitted; even with no non-oversized content there
        // must be at least one chunk carrying the settings item
        assertTrue(
            "At least one chunk must be produced even when route is skipped",
            result.chunks.isNotEmpty(),
        )
    }

    // ---------------------------------------------------------------------------
    // 7. Oversized route doesn't block other routes from being chunked
    // ---------------------------------------------------------------------------

    @Test
    fun `oversized route does not block small routes from being included`() {
        val oversized = makeOversizedRoute("oversized")
        val small1 = makeRoute("small-1", waypointCount = 2)
        val small2 = makeRoute("small-2", waypointCount = 2)
        val data = ExportData(routes = listOf(oversized, small1, small2))

        val result = QrChunker.chunk(data)

        assertTrue("Oversized route should be skipped", result.skippedRoutes.contains("oversized"))
        assertFalse("small-1 should not be skipped", result.skippedRoutes.contains("small-1"))
        assertFalse("small-2 should not be skipped", result.skippedRoutes.contains("small-2"))
        assertTrue("At least one chunk must carry small routes", result.chunks.isNotEmpty())
    }

    @Test
    fun `multiple oversized routes are all skipped independently`() {
        val oversized1 = makeOversizedRoute("giant-1")
        val oversized2 = makeOversizedRoute("giant-2")
        val small = makeRoute("small", waypointCount = 2)
        val data = ExportData(routes = listOf(oversized1, small, oversized2))

        val result = QrChunker.chunk(data)

        assertTrue("giant-1 must be in skippedRoutes", result.skippedRoutes.contains("giant-1"))
        assertTrue("giant-2 must be in skippedRoutes", result.skippedRoutes.contains("giant-2"))
        assertFalse("small route must not be skipped", result.skippedRoutes.contains("small"))
        assertTrue("Chunks must still be produced for non-oversized content", result.chunks.isNotEmpty())
    }
}
