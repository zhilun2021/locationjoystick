package com.locationjoystick.core.location

import com.locationjoystick.core.common.constants.AppConstants
import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationActionsTest {
    @Test
    fun `not replay active - shows Stop Map Favorites`() {
        val actions = selectNotificationActions(replayActive = false, replayPaused = false)
        assertEquals(3, actions.size)
        assertEquals(AppConstants.NotificationConstants.ACTION_STOP, actions[0].label)
        assertEquals(AppConstants.NotificationConstants.ACTION_OPEN_MAP, actions[1].label)
        assertEquals(AppConstants.NotificationConstants.ACTION_OPEN_FAVORITES, actions[2].label)
    }

    @Test
    fun `not replay active with replayPaused true - shows Stop Map Favorites`() {
        // replayPaused=true is meaningless when replayActive=false, but must not crash
        val actions = selectNotificationActions(replayActive = false, replayPaused = true)
        assertEquals(3, actions.size)
        assertEquals(AppConstants.NotificationConstants.ACTION_STOP, actions[0].label)
        assertEquals(AppConstants.NotificationConstants.ACTION_OPEN_MAP, actions[1].label)
        assertEquals(AppConstants.NotificationConstants.ACTION_OPEN_FAVORITES, actions[2].label)
    }

    @Test
    fun `replay active not paused - shows Stop Pause Map`() {
        val actions = selectNotificationActions(replayActive = true, replayPaused = false)
        assertEquals(3, actions.size)
        assertEquals(AppConstants.NotificationConstants.ACTION_STOP, actions[0].label)
        assertEquals(AppConstants.NotificationConstants.ACTION_PAUSE, actions[1].label)
        assertEquals(AppConstants.NotificationConstants.ACTION_OPEN_MAP, actions[2].label)
    }

    @Test
    fun `replay active and paused - shows Stop Resume Map`() {
        val actions = selectNotificationActions(replayActive = true, replayPaused = true)
        assertEquals(3, actions.size)
        assertEquals(AppConstants.NotificationConstants.ACTION_STOP, actions[0].label)
        assertEquals(AppConstants.NotificationConstants.ACTION_RESUME, actions[1].label)
        assertEquals(AppConstants.NotificationConstants.ACTION_OPEN_MAP, actions[2].label)
    }

    @Test
    fun `action keys are correct for non-replay`() {
        val actions = selectNotificationActions(replayActive = false, replayPaused = false)
        assertEquals(AppConstants.ServiceConstants.ACTION_STOP, actions[0].actionKey)
        assertEquals("nav_map", actions[1].actionKey)
        assertEquals("nav_favorites", actions[2].actionKey)
    }

    @Test
    fun `action keys are correct for replay paused`() {
        val actions = selectNotificationActions(replayActive = true, replayPaused = true)
        assertEquals(AppConstants.ServiceConstants.ACTION_STOP, actions[0].actionKey)
        assertEquals(AppConstants.ServiceConstants.ACTION_ROUTE_REPLAY_RESUME, actions[1].actionKey)
        assertEquals("nav_map", actions[2].actionKey)
    }

    @Test
    fun `action keys are correct for replay running`() {
        val actions = selectNotificationActions(replayActive = true, replayPaused = false)
        assertEquals(AppConstants.ServiceConstants.ACTION_STOP, actions[0].actionKey)
        assertEquals(AppConstants.ServiceConstants.ACTION_ROUTE_REPLAY_PAUSE, actions[1].actionKey)
        assertEquals("nav_map", actions[2].actionKey)
    }
}
