package com.locationjoystick.app

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.Box
import com.locationjoystick.app.navigation.LjDrawerContent
import com.locationjoystick.app.navigation.LjNavHost

@Composable
fun LjApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            LjDrawerContent(
                navController = navController,
                drawerState = drawerState
            )
        }
    ) {
        LjNavHost(
            navController = navController,
            drawerState = drawerState
        )
    }
}
