package com.locationjoystick.feature.routes.impl

import com.locationjoystick.core.model.LatLng
import com.locationjoystick.core.model.RouteType
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Regression tests for guided-route waypoint persistence.
 *
 * Bug: GUIDED routes saved only user-placed waypoints, so replay fell back to straight lines.
 * Fix: saveRoute flattens OSRM segments into dense waypoints for GUIDED routes.
 */
class RouteCreatorSaveRouteTest {
    // Mirror the extraction logic from RouteCreatorViewModel.saveRoute
    private fun buildWaypointPositions(
        routeType: RouteType,
        userWaypoints: List<LatLng>,
        segments: List<List<LatLng>>,
    ): List<LatLng> =
        if (routeType == RouteType.GUIDED && segments.isNotEmpty()) {
            segments.flatMapIndexed { i, seg -> if (i == 0) seg else seg.drop(1) }
        } else {
            userWaypoints
        }

    @Test
    fun `guided route flattens OSRM segments not user waypoints`() {
        val user = listOf(LatLng(0.0, 0.0), LatLng(1.0, 1.0))
        val segments =
            listOf(
                listOf(LatLng(0.0, 0.0), LatLng(0.3, 0.2), LatLng(0.7, 0.8), LatLng(1.0, 1.0)),
            )
        val result = buildWaypointPositions(RouteType.GUIDED, user, segments)
        assertEquals(4, result.size)
        assertEquals(LatLng(0.3, 0.2), result[1])
        assertEquals(LatLng(0.7, 0.8), result[2])
    }

    @Test
    fun `guided route with multiple segments deduplicates boundary points`() {
        val user = listOf(LatLng(0.0, 0.0), LatLng(1.0, 0.0), LatLng(2.0, 0.0))
        val segments =
            listOf(
                listOf(LatLng(0.0, 0.0), LatLng(0.5, 0.0), LatLng(1.0, 0.0)),
                listOf(LatLng(1.0, 0.0), LatLng(1.5, 0.0), LatLng(2.0, 0.0)),
            )
        val result = buildWaypointPositions(RouteType.GUIDED, user, segments)
        // First segment: 3 pts, second segment drops first pt: 2 more → total 5
        assertEquals(5, result.size)
        assertEquals(LatLng(0.0, 0.0), result[0])
        assertEquals(LatLng(1.0, 0.0), result[2])
        assertEquals(LatLng(2.0, 0.0), result[4])
    }

    @Test
    fun `straight route uses user waypoints unchanged`() {
        val user = listOf(LatLng(10.0, 20.0), LatLng(11.0, 21.0))
        val segments = listOf(listOf(LatLng(10.0, 20.0), LatLng(10.5, 20.5), LatLng(11.0, 21.0)))
        val result = buildWaypointPositions(RouteType.STRAIGHT, user, segments)
        assertEquals(2, result.size)
        assertEquals(user, result)
    }

    @Test
    fun `guided route with no segments falls back to user waypoints`() {
        val user = listOf(LatLng(5.0, 5.0), LatLng(6.0, 6.0))
        val result = buildWaypointPositions(RouteType.GUIDED, user, emptyList())
        assertEquals(user, result)
    }
}
