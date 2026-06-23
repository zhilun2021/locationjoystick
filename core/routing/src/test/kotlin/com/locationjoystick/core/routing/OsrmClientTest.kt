package com.locationjoystick.core.routing

import com.locationjoystick.core.model.LatLng
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OsrmClientTest {
    private val client = OsrmClient()

    // MockWebServer tests

    private lateinit var server: MockWebServer
    private lateinit var testClient: OsrmClient

    @Before
    fun setUpServer() {
        server = MockWebServer()
        server.start()
        testClient = OsrmClient(server.url("/").toString())
    }

    @After
    fun tearDownServer() {
        server.shutdown()
    }

    @Test
    fun `getRoute returns multi-point polyline on Ok response`() =
        runTest {
            val body =
                """
                {
                  "code": "Ok",
                  "routes": [{
                    "geometry": {
                      "type": "LineString",
                      "coordinates": [
                        [2.3522, 48.8566],
                        [2.3540, 48.8580],
                        [2.3560, 48.8600]
                      ]
                    },
                    "distance": 500.0,
                    "duration": 360.0
                  }]
                }
                """.trimIndent()
            server.enqueue(MockResponse().setResponseCode(200).setBody(body))

            val result =
                testClient.getRoute(
                    profile = "foot",
                    waypoints = listOf(LatLng(48.8566, 2.3522), LatLng(48.8600, 2.3560)),
                )

            assertTrue("Expected success", result.isSuccess)
            val points = result.getOrNull()!!
            assertTrue("Expected more than 2 points from OSRM polyline", points.size > 2)
            // GeoJSON is [lon, lat]; verify parsing swaps them correctly
            assertEquals(48.8566, points[0].latitude, 0.0001)
            assertEquals(2.3522, points[0].longitude, 0.0001)
        }

    @Test
    fun `getRoute returns failure on non-Ok OSRM code`() =
        runTest {
            val body = """{"code": "NoRoute", "routes": []}"""
            // Foot profile failure triggers a driving-profile retry; enqueue a second failure for it.
            server.enqueue(MockResponse().setResponseCode(200).setBody(body))
            server.enqueue(MockResponse().setResponseCode(200).setBody(body))

            val result =
                testClient.getRoute(
                    profile = "foot",
                    waypoints = listOf(LatLng(48.8566, 2.3522), LatLng(48.8600, 2.3560)),
                )

            assertTrue("Expected failure for non-Ok OSRM code", result.isFailure)
        }

    @Test
    fun `getRoute returns failure on HTTP error`() =
        runTest {
            // HTTP errors are classified ServerError and retried (RETRY_COUNT=2) on both the foot
            // profile and its driving-profile fallback: 3 attempts each = 6 requests total.
            repeat(6) { server.enqueue(MockResponse().setResponseCode(400).setBody("Bad Request")) }

            val result =
                testClient.getRoute(
                    profile = "foot",
                    waypoints = listOf(LatLng(48.8566, 2.3522), LatLng(48.8600, 2.3560)),
                )

            assertTrue("Expected failure on HTTP 400", result.isFailure)
            assertEquals("Expected 3 retried foot attempts + 3 retried driving attempts", 6, server.requestCount)
        }

    @Test
    fun `getRoute request uses correct profile and coordinate format`() =
        runTest {
            val body =
                """
                {
                  "code": "Ok",
                  "routes": [{
                    "geometry": {"type": "LineString", "coordinates": [[2.3522, 48.8566], [2.356, 48.86]]},
                    "distance": 100.0,
                    "duration": 60.0
                  }]
                }
                """.trimIndent()
            server.enqueue(MockResponse().setResponseCode(200).setBody(body))

            testClient.getRoute(
                profile = "foot",
                waypoints = listOf(LatLng(48.8566, 2.3522), LatLng(48.8600, 2.3560)),
            )

            val request = server.takeRequest()
            assertTrue("Path should contain profile 'foot'", request.path!!.contains("/foot/"))
            // Coordinates are encoded as lon,lat;lon,lat
            assertTrue("Path should contain coordinates", request.path!!.contains("2.3522,48.8566"))
        }

    @Test
    fun `getRoute retries with driving profile when foot profile fails`() =
        runTest {
            val okBody =
                """
                {
                  "code": "Ok",
                  "routes": [{
                    "geometry": {"type": "LineString", "coordinates": [[2.3522, 48.8566], [2.356, 48.86]]},
                    "distance": 100.0,
                    "duration": 60.0
                  }]
                }
                """.trimIndent()
            // NoRoute (not a retryable reason) so the foot profile fails in a single attempt
            // and falls straight through to the driving-profile fallback.
            server.enqueue(MockResponse().setResponseCode(200).setBody("""{"code": "NoRoute", "routes": []}"""))
            server.enqueue(MockResponse().setResponseCode(200).setBody(okBody))

            val result =
                testClient.getRoute(
                    profile = "foot",
                    waypoints = listOf(LatLng(48.8566, 2.3522), LatLng(48.8600, 2.3560)),
                )

            assertTrue("Expected success after driving-profile retry", result.isSuccess)
            val firstRequest = server.takeRequest()
            val retryRequest = server.takeRequest()
            assertTrue("First request should use 'foot' profile", firstRequest.path!!.contains("/foot/"))
            assertTrue("Retry request should use 'driving' profile", retryRequest.path!!.contains("/driving/"))
        }

    @Test
    fun `getRoute snaps waypoints to nearest road and retries on NoSegment`() =
        runTest {
            val noSegmentBody = """{"code": "NoSegment", "routes": []}"""
            val nearestBody = """{"code": "Ok", "waypoints": [{"location": [2.353, 48.857]}]}"""
            val okBody =
                """
                {
                  "code": "Ok",
                  "routes": [{
                    "geometry": {"type": "LineString", "coordinates": [[2.353, 48.857], [2.356, 48.86]]},
                    "distance": 100.0,
                    "duration": 60.0
                  }]
                }
                """.trimIndent()
            server.enqueue(MockResponse().setResponseCode(200).setBody(noSegmentBody))
            server.enqueue(MockResponse().setResponseCode(200).setBody(nearestBody))
            server.enqueue(MockResponse().setResponseCode(200).setBody(nearestBody))
            server.enqueue(MockResponse().setResponseCode(200).setBody(okBody))

            val result =
                testClient.getRoute(
                    profile = "foot",
                    waypoints = listOf(LatLng(48.8566, 2.3522), LatLng(48.8600, 2.3560)),
                )

            assertTrue("Expected success after snap-to-road retry", result.isSuccess)
            server.takeRequest() // initial route request (NoSegment)
            val nearest1 = server.takeRequest()
            val nearest2 = server.takeRequest()
            val retry = server.takeRequest()
            assertTrue("Expected nearest lookup", nearest1.path!!.contains("/nearest/"))
            assertTrue("Expected nearest lookup", nearest2.path!!.contains("/nearest/"))
            assertTrue("Retry should use route endpoint", retry.path!!.contains("/route/"))
        }

    // Generic retry

    @Test
    fun `getRoute retries on server error and succeeds on third attempt`() =
        runTest {
            val okBody =
                """
                {
                  "code": "Ok",
                  "routes": [{
                    "geometry": {"type": "LineString", "coordinates": [[2.3522, 48.8566], [2.356, 48.86]]},
                    "distance": 100.0,
                    "duration": 60.0
                  }]
                }
                """.trimIndent()
            server.enqueue(MockResponse().setResponseCode(500).setBody("error"))
            server.enqueue(MockResponse().setResponseCode(500).setBody("error"))
            server.enqueue(MockResponse().setResponseCode(200).setBody(okBody))

            val result =
                testClient.getRoute(
                    profile = "foot",
                    waypoints = listOf(LatLng(48.8566, 2.3522), LatLng(48.8600, 2.3560)),
                )

            assertTrue("Expected success on third (final retry) attempt", result.isSuccess)
            assertEquals("Expected exactly 2 retries (3 attempts total)", 3, server.requestCount)
        }

    @Test
    fun `getRoute does not retry NoRouteFound failures`() =
        runTest {
            val body = """{"code": "NoRoute", "routes": []}"""
            server.enqueue(MockResponse().setResponseCode(200).setBody(body))
            server.enqueue(MockResponse().setResponseCode(200).setBody(body))

            val result =
                testClient.getRoute(
                    profile = "foot",
                    waypoints = listOf(LatLng(48.8566, 2.3522), LatLng(48.8600, 2.3560)),
                )

            assertTrue("Expected failure", result.isFailure)
            assertEquals(
                "NoRouteFound must not be retried — exactly 1 foot + 1 driving attempt",
                2,
                server.requestCount,
            )
        }

    // Bisection

    @Test
    fun `bisection is not triggered below the distance threshold`() =
        runTest {
            val body = """{"code": "NoRoute", "routes": []}"""
            server.enqueue(MockResponse().setResponseCode(200).setBody(body))
            server.enqueue(MockResponse().setResponseCode(200).setBody(body))

            // ~600m apart, well under BISECTION_MIN_DISTANCE_METERS (2500m).
            val result =
                testClient.getRoute(
                    profile = "foot",
                    waypoints = listOf(LatLng(48.8566, 2.3522), LatLng(48.8600, 2.3560)),
                )

            assertTrue("Expected failure (no bisection below threshold)", result.isFailure)
            assertEquals("Expected only the direct foot + driving attempts", 2, server.requestCount)
        }

    @Test
    fun `bisection splits a long failing leg and recombines successful halves`() =
        runTest {
            val noRoute = """{"code": "NoRoute", "routes": []}"""

            fun okBody(
                lon: Double,
                lat: Double,
                distance: Double,
            ) = """
                {
                  "code": "Ok",
                  "routes": [{
                    "geometry": {"type": "LineString", "coordinates": [[$lon, $lat], [${lon + 0.001}, ${lat + 0.001}]]},
                    "distance": $distance,
                    "duration": 10.0
                  }]
                }
                """.trimIndent()
            server.enqueue(MockResponse().setResponseCode(200).setBody(noRoute)) // direct foot
            server.enqueue(MockResponse().setResponseCode(200).setBody(noRoute)) // direct driving
            server.enqueue(MockResponse().setResponseCode(200).setBody(okBody(2.3522, 48.8566, 300.0))) // left half
            server.enqueue(MockResponse().setResponseCode(200).setBody(okBody(2.3522, 48.8566, 300.0))) // right half

            // ~4.8km apart, beyond BISECTION_MIN_DISTANCE_METERS (2500m).
            val result =
                testClient.getRoute(
                    profile = "foot",
                    waypoints = listOf(LatLng(48.8566, 2.3522), LatLng(48.9000, 2.3522)),
                )

            assertTrue("Expected bisection to recover a route", result.isSuccess)
            assertEquals("Expected 2 direct attempts + 2 bisected halves", 4, server.requestCount)
        }

    @Test
    fun `bisection recurses deeper when one half keeps failing`() =
        runTest {
            val noRoute = """{"code": "NoRoute", "routes": []}"""
            val okBody =
                """
                {
                  "code": "Ok",
                  "routes": [{
                    "geometry": {"type": "LineString", "coordinates": [[2.3522, 48.8566], [2.353, 48.857]]},
                    "distance": 150.0,
                    "duration": 10.0
                  }]
                }
                """.trimIndent()
            server.enqueue(MockResponse().setResponseCode(200).setBody(noRoute)) // direct foot
            server.enqueue(MockResponse().setResponseCode(200).setBody(noRoute)) // direct driving
            // depth-1 split: one half succeeds, the other fails and must recurse to depth-2
            server.enqueue(MockResponse().setResponseCode(200).setBody(okBody))
            server.enqueue(MockResponse().setResponseCode(200).setBody(noRoute))
            // depth-2 split of the failed half: both sub-halves succeed
            server.enqueue(MockResponse().setResponseCode(200).setBody(okBody))
            server.enqueue(MockResponse().setResponseCode(200).setBody(okBody))

            val result =
                testClient.getRoute(
                    profile = "foot",
                    waypoints = listOf(LatLng(48.8566, 2.3522), LatLng(48.9000, 2.3522)),
                )

            assertTrue("Expected recursive bisection to recover a route", result.isSuccess)
            assertEquals("Expected 2 direct + 2 depth-1 + 2 depth-2 attempts", 6, server.requestCount)
        }

    @Test
    fun `bisection falls back to straight-line past max depth without exceeding bounded request volume`() =
        runTest {
            val noRoute = """{"code": "NoRoute", "routes": []}"""
            // 2 direct + 2 (depth1) + 4 (depth2) + 8 (depth3) + 16 (depth4) + 32 (depth5) = 64.
            // NoRouteFound is never retried, so this is also the exact total request count —
            // well under the ~96 unbounded worst-case the leaf-retry cutoff is meant to avoid.
            repeat(64) { server.enqueue(MockResponse().setResponseCode(200).setBody(noRoute)) }

            val result =
                testClient.getRoute(
                    profile = "foot",
                    waypoints = listOf(LatLng(48.8566, 2.3522), LatLng(48.9000, 2.3522)),
                )

            assertTrue("Max-depth leaves fall back to straight-line, so the overall route still succeeds", result.isSuccess)
            assertEquals("Recursion must stop at depth 5, bounding total request volume", 64, server.requestCount)
        }

    @Test
    fun `bisection time budget is enforced preemptively, not by the underlying call timeout`() =
        runTest {
            val noRoute = """{"code": "NoRoute", "routes": []}"""
            server.enqueue(MockResponse().setResponseCode(200).setBody(noRoute)) // direct foot
            server.enqueue(MockResponse().setResponseCode(200).setBody(noRoute)) // direct driving
            // Both depth-1 halves hang past the 2s bisection budget (but well under the 30s callTimeout).
            server.enqueue(MockResponse().setResponseCode(200).setBody(noRoute).setBodyDelay(4, java.util.concurrent.TimeUnit.SECONDS))
            server.enqueue(MockResponse().setResponseCode(200).setBody(noRoute).setBodyDelay(4, java.util.concurrent.TimeUnit.SECONDS))

            val elapsedMs =
                kotlin.system.measureTimeMillis {
                    val result =
                        testClient.getRoute(
                            profile = "foot",
                            waypoints = listOf(LatLng(48.8566, 2.3522), LatLng(48.9000, 2.3522)),
                        )
                    assertTrue("Expected failure once the bisection budget is exceeded", result.isFailure)
                }

            assertTrue(
                "Expected the call to return near the 2s budget, not the 30s call timeout (took ${elapsedMs}ms)",
                elapsedMs < 10_000,
            )
        }

    // straightLineRoute

    @Test
    fun `straightLineRoute returns two points`() {
        val from = LatLng(0.0, 0.0)
        val to = LatLng(1.0, 1.0)
        val route = client.straightLineRoute(from, to)

        assertEquals(2, route.size)
        assertEquals(from, route[0])
        assertEquals(to, route[1])
    }

    @Test
    fun `straightLineRoute preserves exact coordinates`() {
        val from = LatLng(48.8566, 2.3522)
        val to = LatLng(51.5074, -0.1278)
        val route = client.straightLineRoute(from, to)

        assertEquals(48.8566, route[0].latitude, 0.0001)
        assertEquals(2.3522, route[0].longitude, 0.0001)
        assertEquals(51.5074, route[1].latitude, 0.0001)
        assertEquals(-0.1278, route[1].longitude, 0.0001)
    }

    @Test
    fun `straightLineRoute same start and end returns two identical points`() {
        val point = LatLng(0.0, 0.0)
        val route = client.straightLineRoute(point, point)

        assertEquals(2, route.size)
        assertEquals(point, route[0])
        assertEquals(point, route[1])
    }

    @Test
    fun `straightLineRoute with negative coordinates`() {
        val from = LatLng(-33.8688, 151.2093) // Sydney
        val to = LatLng(-34.0, 151.5)
        val route = client.straightLineRoute(from, to)

        assertEquals(2, route.size)
        assertEquals(-33.8688, route[0].latitude, 0.0001)
        assertEquals(151.2093, route[0].longitude, 0.0001)
    }

    @Test
    fun `straightLineRoute with equator crossing`() {
        val from = LatLng(1.0, 0.0)
        val to = LatLng(-1.0, 0.0)
        val route = client.straightLineRoute(from, to)

        assertEquals(2, route.size)
        assertTrue("first point should be north", route[0].latitude > 0)
        assertTrue("second point should be south", route[1].latitude < 0)
    }

    @Test
    fun `straightLineRoute result is non-null and non-empty`() {
        val from = LatLng(0.0, 0.0)
        val to = LatLng(0.001, 0.001)
        val route = client.straightLineRoute(from, to)

        assertNotNull(route)
        assertTrue(route.isNotEmpty())
    }

    // resolveRoute

    @Test
    fun `resolveRoute with followRoads false returns straight line without network call`() =
        runTest {
            val from = LatLng(48.8566, 2.3522)
            val to = LatLng(51.5074, -0.1278)
            val route = client.resolveRoute("foot", from, to, followRoads = false)

            assertEquals(2, route.size)
            assertEquals(from, route[0])
            assertEquals(to, route[1])
        }

    @Test
    fun `resolveRoute with followRoads true and successful response returns OSRM route`() =
        runTest {
            val body =
                """
                {
                  "code": "Ok",
                  "routes": [{
                    "geometry": {"type": "LineString", "coordinates": [[2.3522, 48.8566], [2.354, 48.858], [2.356, 48.86]]},
                    "distance": 500.0,
                    "duration": 360.0
                  }]
                }
                """.trimIndent()
            server.enqueue(MockResponse().setResponseCode(200).setBody(body))

            val route =
                testClient.resolveRoute(
                    "foot",
                    LatLng(48.8566, 2.3522),
                    LatLng(48.86, 2.356),
                    followRoads = true,
                )

            assertEquals(3, route.size)
        }

    @Test
    fun `resolveRoute with followRoads true falls back to straight line on OSRM failure`() =
        runTest {
            // NoRoute (not retryable) on both foot and driving profile attempts.
            server.enqueue(MockResponse().setResponseCode(200).setBody("""{"code": "NoRoute", "routes": []}"""))
            server.enqueue(MockResponse().setResponseCode(200).setBody("""{"code": "NoRoute", "routes": []}"""))

            // Kept under the bisection distance threshold — this test is about the simple
            // fallback path, not bisection (covered separately below).
            val from = LatLng(48.8566, 2.3522)
            val to = LatLng(48.8600, 2.3560)
            var reportedReason: OsrmFailureReason? = null
            val route = testClient.resolveRoute("foot", from, to, followRoads = true, onFallback = { reportedReason = it })

            assertEquals(2, route.size)
            assertEquals(from, route[0])
            assertEquals(to, route[1])
            assertEquals(OsrmFailureReason.NoRouteFound, reportedReason)
        }

    @Test
    fun `getRoute skips malformed coordinates with fewer than 2 elements`() =
        runTest {
            val body =
                """
                {
                  "code": "Ok",
                  "routes": [{
                    "geometry": {
                      "type": "LineString",
                      "coordinates": [[2.3522, 48.8566], [2.354], [2.356, 48.86]]
                    },
                    "distance": 100.0,
                    "duration": 60.0
                  }]
                }
                """.trimIndent()
            server.enqueue(MockResponse().setResponseCode(200).setBody(body))

            val result =
                testClient.getRoute(
                    profile = "foot",
                    waypoints = listOf(LatLng(48.8566, 2.3522), LatLng(48.86, 2.356)),
                )

            assertTrue(result.isSuccess)
            assertEquals(2, result.getOrNull()!!.size)
        }

    @Test
    fun `getRoute returns failure when routes list is null`() =
        runTest {
            val body = """{"code": "Ok"}"""
            // Foot profile failure triggers a driving-profile retry; enqueue a second failure for it.
            server.enqueue(MockResponse().setResponseCode(200).setBody(body))
            server.enqueue(MockResponse().setResponseCode(200).setBody(body))

            val result =
                testClient.getRoute(
                    profile = "foot",
                    waypoints = listOf(LatLng(48.8566, 2.3522), LatLng(48.86, 2.356)),
                )

            assertTrue("Expected failure when routes is null", result.isFailure)
        }

    // PROFILE_FOOT constant

    @Test
    fun `OsrmClient PROFILE_FOOT constant is defined`() {
        assertTrue(OsrmClient.PROFILE_FOOT.isNotEmpty())
    }

    // Live integration test — requires network; validates real OSRM round-trip

    @Test
    fun `getRoute live - two Paris points return road-following polyline`() =
        runTest {
            val from = LatLng(48.8566, 2.3522) // Notre-Dame
            val to = LatLng(48.8606, 2.3376) // Louvre
            val result = client.getRoute(profile = OsrmClient.PROFILE_FOOT, waypoints = listOf(from, to))

            assertTrue("Live OSRM call failed: ${result.exceptionOrNull()}", result.isSuccess)
            val points = result.getOrNull()!!
            assertTrue("Expected road-following polyline (>2 points), got ${points.size}", points.size > 2)
        }
}
