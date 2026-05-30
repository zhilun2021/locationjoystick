package com.locationjoystick.app.smoke

import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.locationjoystick.app.MainActivity

typealias SmokeRule = AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>

fun SmokeRule.waitForIdleScreen() {
    activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
    waitForIdle()
    waitForIdle()
}

fun SmokeRule.openDrawer() {
    onNodeWithContentDescription("Open navigation menu").performClick()
    waitForIdle()
}

fun SmokeRule.navigateViaDrawer(label: String) {
    openDrawer()
    onAllNodesWithText(label)
        .filterToOne(hasAnyAncestor(hasTestTag("nav_drawer")))
        .performClick()
    waitForIdle()
}

fun SmokeRule.navigateFromIdle(cardLabel: String) {
    onAllNodesWithText(cardLabel)
        .filterToOne(!hasAnyAncestor(hasTestTag("nav_drawer")))
        .performClick()
    waitForIdle()
}
