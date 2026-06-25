package com.locationjoystick.feature.widget.impl

import com.locationjoystick.core.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class TapToWalkCoordinateTest {
    private val center = LatLng(0.0, 0.0)
    private val screenW = 1080
    private val screenH = 1920
    private val scale = 10.0 // 10 m/px

    @Test
    fun tapCenter_returnsCurrentPosition() {
        val result =
            TapToWalkOverlay.computeWalkTarget(
                center,
                screenW / 2f,
                screenH / 2f,
                screenW,
                screenH,
                scale,
            )
        assertEquals(0.0, result.latitude, 1e-6)
        assertEquals(0.0, result.longitude, 1e-6)
    }

    @Test
    fun tapRight_increasesLongitude() {
        val result =
            TapToWalkOverlay.computeWalkTarget(
                center,
                screenW * 0.75f,
                screenH / 2f,
                screenW,
                screenH,
                scale,
            )
        assertTrue(result.longitude > 0.0)
        assertEquals(0.0, result.latitude, 1e-6)
    }

    @Test
    fun tapLeft_decreasesLongitude() {
        val result =
            TapToWalkOverlay.computeWalkTarget(
                center,
                screenW * 0.25f,
                screenH / 2f,
                screenW,
                screenH,
                scale,
            )
        assertTrue(result.longitude < 0.0)
        assertEquals(0.0, result.latitude, 1e-6)
    }

    @Test
    fun tapAboveCenter_increasesLatitude() {
        val result =
            TapToWalkOverlay.computeWalkTarget(
                center,
                screenW / 2f,
                screenH * 0.25f,
                screenW,
                screenH,
                scale,
            )
        assertTrue(result.latitude > 0.0)
        assertEquals(0.0, result.longitude, 1e-6)
    }

    @Test
    fun tapBelowCenter_decreasesLatitude() {
        val result =
            TapToWalkOverlay.computeWalkTarget(
                center,
                screenW / 2f,
                screenH * 0.75f,
                screenW,
                screenH,
                scale,
            )
        assertTrue(result.latitude < 0.0)
        assertEquals(0.0, result.longitude, 1e-6)
    }

    @Test
    fun largerScale_largerDisplacement() {
        val small =
            TapToWalkOverlay.computeWalkTarget(
                center,
                screenW * 0.75f,
                screenH / 2f,
                screenW,
                screenH,
                1.0,
            )
        val large =
            TapToWalkOverlay.computeWalkTarget(
                center,
                screenW * 0.75f,
                screenH / 2f,
                screenW,
                screenH,
                50.0,
            )
        assertTrue(abs(large.longitude) > abs(small.longitude))
    }

    @Test
    fun latitudeClampedTo90() {
        // Tap far above center with huge scale to force lat > 90
        val result =
            TapToWalkOverlay.computeWalkTarget(
                LatLng(89.0, 0.0),
                screenW / 2f,
                0f,
                screenW,
                screenH,
                50.0,
            )
        assertTrue(result.latitude <= 90.0)
    }

    @Test
    fun displacementSymmetry() {
        // Right tap and left tap from equator should have equal-magnitude longitude shifts
        val right =
            TapToWalkOverlay.computeWalkTarget(
                center,
                screenW * 0.75f,
                screenH / 2f,
                screenW,
                screenH,
                scale,
            )
        val left =
            TapToWalkOverlay.computeWalkTarget(
                center,
                screenW * 0.25f,
                screenH / 2f,
                screenW,
                screenH,
                scale,
            )
        assertEquals(right.longitude, -left.longitude, 1e-9)
    }
}
