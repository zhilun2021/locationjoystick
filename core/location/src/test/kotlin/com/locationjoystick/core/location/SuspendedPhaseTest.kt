package com.locationjoystick.core.location

import com.locationjoystick.core.common.constants.AppConstants
import com.locationjoystick.core.model.MockMode
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SuspendedPhaseTest {
    private val pushDur = AppConstants.RealismConstants.SUSPENDED_PUSH_DURATION_MS
    private val pauseDur = AppConstants.RealismConstants.SUSPENDED_PAUSE_DURATION_MS
    private val jitter = AppConstants.RealismConstants.SUSPENDED_PAUSE_JITTER_MS

    private fun pushing(startMs: Long = 0L) = SuspendedPhaseState(isActive = false, startMs = startMs)

    private fun paused(startMs: Long = 0L) = SuspendedPhaseState(isActive = true, startMs = startMs)

    // -------------------------------------------------------------------------
    // Push → Paused transition
    // -------------------------------------------------------------------------

    @Test
    fun `push duration elapsed transitions from pushing to paused`() {
        val start = pushing(startMs = 0L)
        val next = advanceSuspendedPhase(start, now = pushDur, enabled = true, mode = MockMode.TELEPORT, random = Random(1))
        assertTrue("Should be paused after push duration", next.isActive)
    }

    @Test
    fun `push duration not yet elapsed stays pushing`() {
        val start = pushing(startMs = 0L)
        val next = advanceSuspendedPhase(start, now = pushDur - 1, enabled = true, mode = MockMode.TELEPORT, random = Random(1))
        assertFalse("Should still be pushing before push duration", next.isActive)
    }

    // -------------------------------------------------------------------------
    // Paused → Pushing transition
    // -------------------------------------------------------------------------

    @Test
    fun `pause duration elapsed transitions from paused to pushing`() {
        val start = paused(startMs = 0L)
        // Use seed that gives 0 jitter to make this deterministic
        val next = advanceSuspendedPhase(start, now = pauseDur, enabled = true, mode = MockMode.TELEPORT, random = Random.Default)
        // pauseDur is the minimum; with any jitter >= 0 the elapsed time might not be enough.
        // Use pauseDur + jitter to guarantee the transition regardless of jitter value.
        val nextAtMax =
            advanceSuspendedPhase(start, now = pauseDur + jitter, enabled = true, mode = MockMode.TELEPORT, random = Random.Default)
        assertFalse("Should be pushing after pause + full jitter duration", nextAtMax.isActive)
    }

    @Test
    fun `pause duration not elapsed stays paused`() {
        val start = paused(startMs = 0L)
        val next = advanceSuspendedPhase(start, now = pauseDur - 1, enabled = true, mode = MockMode.TELEPORT, random = Random(1))
        assertTrue("Should still be paused before pause duration", next.isActive)
    }

    // -------------------------------------------------------------------------
    // Mode gating: ROUTE_REPLAY and WALK_TO disable the phase machine
    // -------------------------------------------------------------------------

    @Test
    fun `phase does not transition in ROUTE_REPLAY mode`() {
        val start = pushing(startMs = 0L)
        val next = advanceSuspendedPhase(start, now = pushDur * 10, enabled = true, mode = MockMode.ROUTE_REPLAY, random = Random(1))
        assertFalse("ROUTE_REPLAY must disable suspended phase", next.isActive)
    }

    @Test
    fun `phase does not transition in WALK_TO mode`() {
        val start = pushing(startMs = 0L)
        val next = advanceSuspendedPhase(start, now = pushDur * 10, enabled = true, mode = MockMode.WALK_TO, random = Random(1))
        assertFalse("WALK_TO must disable suspended phase", next.isActive)
    }

    @Test
    fun `paused phase resets to pushing when mode is ROUTE_REPLAY`() {
        val start = paused(startMs = 0L)
        val next = advanceSuspendedPhase(start, now = 0L, enabled = true, mode = MockMode.ROUTE_REPLAY, random = Random(1))
        assertFalse("ROUTE_REPLAY must clear paused state", next.isActive)
    }

    // -------------------------------------------------------------------------
    // Disabled
    // -------------------------------------------------------------------------

    @Test
    fun `disabled suspended mocking always returns pushing`() {
        val start = paused(startMs = 0L)
        val next = advanceSuspendedPhase(start, now = pauseDur * 100, enabled = false, mode = MockMode.TELEPORT, random = Random(1))
        assertFalse("Disabled mocking must clear paused state", next.isActive)
    }

    // -------------------------------------------------------------------------
    // Jitter bounds
    // -------------------------------------------------------------------------

    @Test
    fun `jitter value stays within SUSPENDED_PAUSE_JITTER_MS bounds`() {
        val start = paused(startMs = 0L)
        // Find the minimum and maximum 'now' that causes a transition across 100 seeds.
        // The actual pause duration = pauseDur + jitter_sample where jitter_sample in [0, JITTER).
        // We verify that no transition happens before pauseDur and all happen by pauseDur + JITTER.
        repeat(100) { seed ->
            val noTransition =
                advanceSuspendedPhase(start, now = pauseDur - 1, enabled = true, mode = MockMode.TELEPORT, random = Random(seed))
            assertTrue("Should still be paused at pauseDur - 1 regardless of jitter", noTransition.isActive)

            val alwaysTransitioned =
                advanceSuspendedPhase(start, now = pauseDur + jitter, enabled = true, mode = MockMode.TELEPORT, random = Random(seed))
            assertFalse("Should have transitioned to pushing by pauseDur + full jitter", alwaysTransitioned.isActive)
        }
    }
}
