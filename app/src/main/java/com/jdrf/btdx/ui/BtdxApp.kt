package com.jdrf.btdx.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jdrf.btdx.ext.iLog
import com.jdrf.btdx.ui.details.DeviceDetailsScreen
import com.jdrf.btdx.ui.dtdisabled.BluetoothDisabledScreen
import com.jdrf.btdx.ui.navigation.BtdxDestinations
import com.jdrf.btdx.ui.navigation.BtdxDestinationsArgs.MAC_ADDRESS_ARG
import com.jdrf.btdx.ui.navigation.BtdxNavigationActions
import com.jdrf.btdx.ui.nosupport.ApplicationNotSupportedScreen
import com.jdrf.btdx.ui.permissions.PermissionsScreen
import com.jdrf.btdx.ui.scanner.DeviceScannerScreen

@Composable
fun BtdxApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = BtdxDestinations.SCANNED_DEVICES_ROUTE,
    isBluetoothAvailable: Boolean,
    isAllPermissionsGranted: Boolean,
    isBluetoothTurnedOn: Boolean,
    navActions: BtdxNavigationActions = remember(
        navController,
        isAllPermissionsGranted,
        startDestination,
        isBluetoothAvailable,
        isBluetoothTurnedOn
    ) {
        BtdxNavigationActions(navController, startDestination)
    },
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.background,
            )
            .padding(horizontal = 5.dp)
    ) { innerPadding ->
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
                val macAddress = entry.arguments?.getString(MAC_ADDRESS_ARG).also {
                    iLog("show details for device with mac: $it")
                }

                DeviceDetailsScreen(
                    macAddress = macAddress
                )
            }
            composable(BtdxDestinations.APPLICATION_NOT_SUPPORTED_ROUTE) {
                ApplicationNotSupportedScreen()
            }
            composable(BtdxDestinations.PERMISSIONS_ROUTE) {
                PermissionsScreen {
                    if (!isBluetoothTurnedOn) {
                        navActions.navigateToBluetoothDisabledRoute()
                    } else {
                        navActions.navigateToStartDestinationScreen()
                    }
                }
            }
            composable(BtdxDestinations.BLUETOOTH_DISABLED_ROUTE) {
                BluetoothDisabledScreen {
                    if (!isAllPermissionsGranted) {
                        navActions.navigateToPermissionsScreen()
                    } else {
                        navActions.navigateToScannedDevices()
                    }
                }
            }
        }

        if (!isBluetoothAvailable) {
            navActions.navigateToApplicationNotSupported()
        } else if (!isAllPermissionsGranted) {
            navActions.navigateToPermissionsScreen()
        } else if (!isBluetoothTurnedOn) {
            navActions.navigateToBluetoothDisabledRoute()
        }
    }
}
