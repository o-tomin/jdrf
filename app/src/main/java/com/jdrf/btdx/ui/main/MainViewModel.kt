package com.jdrf.btdx.ui.main

import com.jdrf.btdx.bluetooth.feature.BluetoothFeature
import com.jdrf.btdx.ui.MviBaseViewModel
import com.jdrf.btdx.ui.MviBaseViewState
import com.jdrf.btdx.ui.navigation.BtdxDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    bluetoothFeature: BluetoothFeature
) : MviBaseViewModel<MainState, Any>(
    initialState = MainState(
        startDestination = startDestination(bluetoothFeature.isAvailable),
    )
) {

    companion object {

        private fun startDestination(isBluetoothAvailable: Boolean): String {
            return if (isBluetoothAvailable) {
                BtdxDestinations.SCANNED_DEVICES_ROUTE
            } else {
                BtdxDestinations.APPLICATION_NOT_SUPPORTED_ROUTE
            }
        }
    }
}

data class MainState(
    val startDestination: String,
) : MviBaseViewState
