package com.jdrf.btdx.ui.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import com.jdrf.btdx.ui.navigation.BtdxDestinationsArgs.MAC_ADDRESS_ARG
import com.jdrf.btdx.ui.navigation.BtdxScreens.APPLICATION_NOT_SUPPORTED_SCREEN
import com.jdrf.btdx.ui.navigation.BtdxScreens.DEVICE_DETAILS_SCREEN
import com.jdrf.btdx.ui.navigation.BtdxScreens.SCANNED_DEVICES_SCREEN

private object BtdxScreens {
    const val APPLICATION_NOT_SUPPORTED_SCREEN = "applicationNotSupported"
    const val SCANNED_DEVICES_SCREEN = "scannedDevices"
    const val DEVICE_DETAILS_SCREEN = "deviceDetails"
}

object BtdxDestinationsArgs {
    const val MAC_ADDRESS_ARG = "macAddress"
}

object BtdxDestinations {
    const val APPLICATION_NOT_SUPPORTED_ROUTE = APPLICATION_NOT_SUPPORTED_SCREEN
    const val DEVICE_DETAILS_ROUTE = "$DEVICE_DETAILS_SCREEN/?{$MAC_ADDRESS_ARG}"
    const val SCANNED_DEVICES_ROUTE = SCANNED_DEVICES_SCREEN
}

class BtdxNavigationActions(private val navController: NavHostController) {

    fun navigateToDeviceDetails(
        macAddress: String?
    ) {
        navController.navigate("${DEVICE_DETAILS_SCREEN}/?$macAddress") {
            defaultNavOptions()
        }
    }

    fun navigateToApplicationNotSupported() {
        navController.navigate(APPLICATION_NOT_SUPPORTED_SCREEN) {
            defaultNavOptions()
        }
    }

    private fun NavOptionsBuilder.defaultNavOptions() {
        // Pop up to the start destination of the graph to
        // avoid building up a large stack of destinations
        // on the back stack as users select items
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        // Avoid multiple copies of the same destination when
        // reselecting the same item
        launchSingleTop = true
        // Restore state when reselecting a previously selected item
        restoreState = true
    }
}
