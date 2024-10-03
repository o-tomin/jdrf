package com.jdrf.btdx.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jdrf.btdx.ui.details.DeviceDetailsScreen
import com.jdrf.btdx.ui.navigation.BtdxDestinations
import com.jdrf.btdx.ui.navigation.BtdxDestinationsArgs.MAC_ADDRESS_ARG
import com.jdrf.btdx.ui.navigation.BtdxNavigationActions
import com.jdrf.btdx.ui.scanner.DeviceScannerScreen

@Composable
fun BtdxApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = BtdxDestinations.SCANNED_DEVICES_ROUTE,
    navActions: BtdxNavigationActions = remember(navController) {
        BtdxNavigationActions(navController)
    }
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(BtdxDestinations.SCANNED_DEVICES_ROUTE) {
                DeviceScannerScreen(
                    onDeviceDetails = {
                        navActions.navigateToDeviceDetails(
                            macAddress = it
                        )
                    }
                )
            }
            composable(
                BtdxDestinations.DEVICE_DETAILS_ROUTE,
                arguments = listOf(
                    navArgument(MAC_ADDRESS_ARG) {
                        type = NavType.StringType
                        nullable = true
                    }
                )
            ) { entry ->
                val macAddress = entry.arguments?.getString(MAC_ADDRESS_ARG)

                DeviceDetailsScreen(
                    macAddress = macAddress
                )
            }
        }
    }
}
