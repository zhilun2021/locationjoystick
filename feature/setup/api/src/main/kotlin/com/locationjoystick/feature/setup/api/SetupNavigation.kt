package com.locationjoystick.feature.setup.api

import androidx.navigation.NavController
import androidx.navigation.NavOptions

const val SETUP_ROUTE = "setup"

fun NavController.navigateToSetup(navOptions: NavOptions? = null) {
    navigate(SETUP_ROUTE, navOptions)
}
