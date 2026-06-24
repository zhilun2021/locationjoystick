package com.locationjoystick.app.smoke

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Test

@HiltAndroidTest
class FloatingWidgetSmokeTest : BaseSmokeTest() {
    @Before
    override fun setup() {
        super.setup()
        composeRule.waitForIdleScreen()
        composeRule.navigateFromIdle("Settings")
        composeRule.onNodeWithText("Menus").performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun app_features_section_is_displayed() {
        composeRule.onNodeWithText("App Features").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun app_features_shows_map_shortcut_feature() {
        composeRule.onNodeWithText("Map shortcut").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun app_features_shows_joystick_toggle_feature() {
        composeRule.onNodeWithText("Show/hide joystick").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun app_features_shows_joystick_lock_feature() {
        composeRule.onNodeWithText("Lock joystick").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun app_features_shows_routes_feature() {
        composeRule.onNodeWithText("Lists saved routes and starts replay.").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun app_features_shows_favorites_feature() {
        composeRule.onNodeWithText("Teleport or walk to a saved location.").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun app_features_shows_speed_cycle_feature() {
        composeRule.onNodeWithText("Speed cycle").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun app_features_widget_toggle_no_crash() {
        composeRule.onNodeWithContentDescription("Routes on widget").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Routes on widget").performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun app_features_map_toggle_no_crash() {
        composeRule.onNodeWithContentDescription("Favorites on map").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Favorites on map").performClick()
        composeRule.waitForIdle()
    }
}
