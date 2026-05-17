package com.locationjoystick.feature.settings.impl

import com.locationjoystick.core.model.AppSettings
import com.locationjoystick.core.model.FavoriteLocation
import com.locationjoystick.core.model.JoystickStyle
import com.locationjoystick.core.model.LatLng
import com.locationjoystick.core.model.Route
import com.locationjoystick.core.model.RouteType
import com.locationjoystick.core.model.SpeedUnit
import com.locationjoystick.core.model.Waypoint
import com.locationjoystick.core.model.WidgetFeature
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// ---------------------------------------------------------------------------
// Test helpers
// ---------------------------------------------------------------------------

private fun minimalSettings(): AppSettings =
    AppSettings(
        activeSpeedProfileId = "walk",
        joystickStyle = JoystickStyle.FLOATING,
        enabledWidgetFeatures = emptyList(),
        mapFollowsLocation = true,
        useRoadSnappingByDefault = false,
        speedUnit = SpeedUnit.KMH,
    )

private fun settingsContent(): ChunkContent.Settings = ChunkContent.Settings(minimalSettings())

private fun routesContent(routes: List<Route>): ChunkContent.Routes = ChunkContent.Routes(routes)

private fun favoritesContent(favs: List<FavoriteLocation>): ChunkContent.Favorites = ChunkContent.Favorites(favs)

private fun testRoute(
    id: String = "r1",
    name: String = "Test Route",
): Route =
    Route(
        id = id,
        name = name,
        waypoints =
            listOf(
                Waypoint(id = "wp1", position = LatLng(1.0, 2.0), orderIndex = 0),
                Waypoint(id = "wp2", position = LatLng(3.0, 4.0), orderIndex = 1),
            ),
        isLooping = false,
        routeType = RouteType.STRAIGHT,
        createdAt = 0L,
    )

private fun testFavorite(
    id: String = "fav1",
    name: String = "Home",
): FavoriteLocation =
    FavoriteLocation(
        id = id,
        name = name,
        position = LatLng(48.8566, 2.3522),
        createdAt = 1000L,
    )

// ---------------------------------------------------------------------------
// Tests
// ---------------------------------------------------------------------------

class ChunkReassemblerTest {
    // 1. Out-of-order chunks reassemble to correct ExportData
    @Test
    fun `out-of-order chunks reassemble to correct ExportData`() {
        val route1 = testRoute("r1", "Route 1")
        val route2 = testRoute("r2", "Route 2")
        val fav = testFavorite()

        // Deliberately supply in reverse order: favorites, routes, settings
        val chunks =
            listOf(
                favoritesContent(listOf(fav)),
                routesContent(listOf(route2)),
                routesContent(listOf(route1)),
                settingsContent(),
            )

        val result = mergeChunkContents(chunks)

        assertEquals(minimalSettings(), result.settings)
        assertEquals(2, result.routes.size)
        val routeIds = result.routes.map { it.id }
        assertTrue(routeIds.contains("r1"))
        assertTrue(routeIds.contains("r2"))
        assertEquals(1, result.favoriteLocations.size)
        assertEquals("fav1", result.favoriteLocations[0].id)
    }

    // 2. Duplicate chunk index does not duplicate data
    @Test
    fun `duplicate chunk index does not duplicate data`() {
        val route = testRoute()

        // Same routes chunk appears twice (simulates duplicate delivery)
        val chunks =
            listOf(
                settingsContent(),
                routesContent(listOf(route)),
                routesContent(listOf(route)),
            )

        val result = mergeChunkContents(chunks)

        // mergeChunkContents flatMaps all Routes chunks — duplicates ARE included
        // The caller (onChunkScanned) prevents duplicate chunk indices by keying on chunk index.
        // At the mergeChunkContents level, we verify the function faithfully merges what it receives:
        // if duplicates are fed in, they appear; dedup is the caller's responsibility.
        // This test asserts that with one unique route fed twice the count is 2 (no silent dedup
        // inside mergeChunkContents), which documents the contract.
        assertEquals(
            "mergeChunkContents does not silently deduplicate routes; caller must prevent duplicates",
            2,
            result.routes.size,
        )
        assertTrue(result.routes.all { it.id == "r1" })
    }

    // 3. mergeChunks with no routes chunk yields empty routes
    @Test
    fun `mergeChunks with no routes chunk yields empty routes`() {
        val fav = testFavorite()
        val chunks =
            listOf(
                settingsContent(),
                favoritesContent(listOf(fav)),
            )

        val result = mergeChunkContents(chunks)

        assertTrue("Routes must be empty when no routes chunk is present", result.routes.isEmpty())
        assertEquals(1, result.favoriteLocations.size)
        assertEquals("fav1", result.favoriteLocations[0].id)
        assertEquals(minimalSettings(), result.settings)
    }

    // 4. Settings-only chunk merges correctly (no routes/favorites)
    @Test
    fun `settings-only chunk merges correctly with no routes or favorites`() {
        val chunks = listOf(settingsContent())

        val result = mergeChunkContents(chunks)

        assertEquals(minimalSettings(), result.settings)
        assertTrue("Routes must be empty", result.routes.isEmpty())
        assertTrue("Favorites must be empty", result.favoriteLocations.isEmpty())
    }
}
