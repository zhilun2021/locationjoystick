package com.locationjoystick.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SpeedProfileTest {
    @Test
    fun `defaultProfiles returns exactly three profiles`() {
        assertEquals(3, SpeedProfile.defaultProfiles().size)
    }

    @Test
    fun `defaultProfiles contains walk run bike ids`() {
        val ids = SpeedProfile.defaultProfiles().map { it.id }.toSet()
        assertEquals(setOf("walk", "run", "bike"), ids)
    }

    @Test
    fun `defaultProfiles walk speed is 2 kmh`() {
        val walk = SpeedProfile.defaultProfiles().first { it.id == "walk" }
        assertEquals(0.5556, walk.speedMetersPerSecond, 0.001)
    }

    @Test
    fun `defaultProfiles run speed is 8 kmh`() {
        val run = SpeedProfile.defaultProfiles().first { it.id == "run" }
        assertEquals(2.2222, run.speedMetersPerSecond, 0.001)
    }

    @Test
    fun `defaultProfiles bike speed is 15 kmh`() {
        val bike = SpeedProfile.defaultProfiles().first { it.id == "bike" }
        assertEquals(4.1667, bike.speedMetersPerSecond, 0.001)
    }

    @Test
    fun `defaultProfiles walk is slower than run`() {
        val profiles = SpeedProfile.defaultProfiles().associateBy { it.id }
        assert(profiles["walk"]!!.speedMetersPerSecond < profiles["run"]!!.speedMetersPerSecond)
    }

    @Test
    fun `defaultProfiles run is slower than bike`() {
        val profiles = SpeedProfile.defaultProfiles().associateBy { it.id }
        assert(profiles["run"]!!.speedMetersPerSecond < profiles["bike"]!!.speedMetersPerSecond)
    }
}
