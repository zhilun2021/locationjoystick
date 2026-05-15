package com.locationjoystick.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LatLngTest {
    // distanceTo

    @Test
    fun `distanceTo same point returns zero`() {
        val point = LatLng(48.8566, 2.3522)
        assertEquals(0.0, point.distanceTo(point), 0.001)
    }

    @Test
    fun `distanceTo 1 degree latitude is approx 111km`() {
        val a = LatLng(0.0, 0.0)
        val b = LatLng(1.0, 0.0)
        assertEquals(111_195.0, a.distanceTo(b), 500.0)
    }

    @Test
    fun `distanceTo 1 degree longitude at equator is approx 111km`() {
        val a = LatLng(0.0, 0.0)
        val b = LatLng(0.0, 1.0)
        assertEquals(111_195.0, a.distanceTo(b), 500.0)
    }

    @Test
    fun `distanceTo is symmetric`() {
        val a = LatLng(51.5074, -0.1278)
        val b = LatLng(48.8566, 2.3522)
        assertEquals(a.distanceTo(b), b.distanceTo(a), 0.001)
    }

    @Test
    fun `distanceTo London to Paris is approx 340km`() {
        val london = LatLng(51.5074, -0.1278)
        val paris = LatLng(48.8566, 2.3522)
        assertEquals(340_000.0, london.distanceTo(paris), 5_000.0)
    }

    @Test
    fun `distanceTo short distance 100m is accurate`() {
        // 100m north: 0.0009° latitude ≈ 100m
        val a = LatLng(0.0, 0.0)
        val b = LatLng(0.0009, 0.0)
        assertEquals(100.0, a.distanceTo(b), 2.0)
    }

    // bearingTo

    @Test
    fun `bearingTo due north returns 0`() {
        val a = LatLng(0.0, 0.0)
        val b = LatLng(1.0, 0.0)
        assertEquals(0.0, a.bearingTo(b), 0.1)
    }

    @Test
    fun `bearingTo due east returns 90`() {
        val a = LatLng(0.0, 0.0)
        val b = LatLng(0.0, 1.0)
        assertEquals(90.0, a.bearingTo(b), 0.1)
    }

    @Test
    fun `bearingTo due south returns 180`() {
        val a = LatLng(1.0, 0.0)
        val b = LatLng(0.0, 0.0)
        assertEquals(180.0, a.bearingTo(b), 0.1)
    }

    @Test
    fun `bearingTo due west returns 270`() {
        val a = LatLng(0.0, 1.0)
        val b = LatLng(0.0, 0.0)
        assertEquals(270.0, a.bearingTo(b), 0.1)
    }

    @Test
    fun `bearingTo result is in range 0 to 360`() {
        val a = LatLng(51.5, 0.0)
        val b = LatLng(40.7, -74.0)
        val bearing = a.bearingTo(b)
        assertTrue("bearing $bearing out of range [0,360)", bearing >= 0.0 && bearing < 360.0)
    }

    @Test
    fun `bearingTo northeast is between 0 and 90`() {
        val a = LatLng(0.0, 0.0)
        val b = LatLng(1.0, 1.0)
        val bearing = a.bearingTo(b)
        assertTrue("expected NE bearing, got $bearing", bearing > 0.0 && bearing < 90.0)
    }

    @Test
    fun `bearingTo southeast is between 90 and 180`() {
        val a = LatLng(1.0, 0.0)
        val b = LatLng(0.0, 1.0)
        val bearing = a.bearingTo(b)
        assertTrue("expected SE bearing, got $bearing", bearing > 90.0 && bearing < 180.0)
    }

    @Test
    fun `bearingTo southwest is between 180 and 270`() {
        val a = LatLng(1.0, 1.0)
        val b = LatLng(0.0, 0.0)
        val bearing = a.bearingTo(b)
        assertTrue("expected SW bearing, got $bearing", bearing > 180.0 && bearing < 270.0)
    }

    @Test
    fun `bearingTo northwest is between 270 and 360`() {
        val a = LatLng(0.0, 1.0)
        val b = LatLng(1.0, 0.0)
        val bearing = a.bearingTo(b)
        assertTrue("expected NW bearing, got $bearing", bearing > 270.0 && bearing < 360.0)
    }

    @Test
    fun `distanceTo obeys triangle inequality`() {
        val a = LatLng(0.0, 0.0)
        val b = LatLng(1.0, 0.0)
        val c = LatLng(1.0, 1.0)
        val ab = a.distanceTo(b)
        val bc = b.distanceTo(c)
        val ac = a.distanceTo(c)
        assertTrue("a→c should be less than a→b + b→c",
            ac <= ab + bc + 1.0) // +1.0 for numerical error
    }

    @Test
    fun `distanceTo zero distance for nearby points`() {
        val a = LatLng(0.0, 0.0)
        val b = LatLng(0.00001, 0.0) // ~1.1m away
        val dist = a.distanceTo(b)
        assertTrue("should be under 2m", dist < 2.0)
        assertTrue("should be positive", dist > 0.0)
    }

    @Test
    fun `bearingTo and distanceTo are independent`() {
        val a = LatLng(48.8566, 2.3522)
        val b = LatLng(51.5, -0.1)
        val bearing = a.bearingTo(b)
        val distance = a.distanceTo(b)
        // Bearing should be valid regardless of distance
        assertTrue("bearing should be valid", bearing >= 0.0 && bearing < 360.0)
        assertTrue("distance should be positive", distance > 0.0)
    }
}
