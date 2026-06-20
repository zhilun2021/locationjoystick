package com.locationjoystick.core.data

import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GoogleMapsShortLinkResolverTest {
    private lateinit var server: MockWebServer
    private lateinit var resolver: GoogleMapsShortLinkResolver

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        resolver = GoogleMapsShortLinkResolver()
        resolver.client =
            OkHttpClient
                .Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .build()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `isShortLink returns true for goo gl host`() {
        assertTrue(resolver.isShortLink("https://goo.gl/maps/xxxx"))
    }

    @Test
    fun `isShortLink returns true for maps app goo gl host`() {
        assertTrue(resolver.isShortLink("https://maps.app.goo.gl/xxxx"))
    }

    @Test
    fun `isShortLink returns false for other hosts`() {
        assertFalse(resolver.isShortLink("https://maps.google.com/maps?q=35.62,139.77"))
    }

    @Test
    fun `isShortLink returns false for unparseable url`() {
        assertFalse(resolver.isShortLink("not a url"))
    }

    @Test
    fun `resolve follows redirect chain and returns final url`() =
        runTest {
            val finalUrl = server.url("/maps/place/Name/@1.0,2.0,15z/data=!3d50.305571!4d2.792041")
            server.enqueue(
                MockResponse()
                    .setResponseCode(302)
                    .setHeader("Location", finalUrl.toString()),
            )
            server.enqueue(MockResponse().setResponseCode(200))

            val result = resolver.resolve(server.url("/maps/xxxx").toString())

            assertEquals(finalUrl.toString(), result)
        }

    @Test
    fun `resolve follows multiple redirect hops`() =
        runTest {
            val intermediateUrl = server.url("/intermediate")
            val finalUrl = server.url("/final?lat=35.62&lon=139.77")
            server.enqueue(
                MockResponse()
                    .setResponseCode(302)
                    .setHeader("Location", intermediateUrl.toString()),
            )
            server.enqueue(
                MockResponse()
                    .setResponseCode(302)
                    .setHeader("Location", finalUrl.toString()),
            )
            server.enqueue(MockResponse().setResponseCode(200))

            val result = resolver.resolve(server.url("/start").toString())

            assertEquals(finalUrl.toString(), result)
        }

    @Test
    fun `resolve returns same url when no redirect occurs`() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(200))

            val url = server.url("/no-redirect").toString()
            val result = resolver.resolve(url)

            assertEquals(url, result)
        }

    @Test
    fun `resolve returns null when request fails`() =
        runTest {
            server.shutdown()

            val result = resolver.resolve(server.url("/unreachable").toString())

            assertNull(result)
        }
}
