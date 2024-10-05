package com.jdrf.btdx.ui.main

import android.bluetooth.BluetoothAdapter
import androidx.lifecycle.viewModelScope
import com.jdrf.btdx.bluetooth.feature.BluetoothFeature
import com.jdrf.btdx.bluetooth.feature.BluetoothPermissionsObserver
import com.jdrf.btdx.bluetooth.feature.BluetoothPermissionsObserver.Companion.allPermissionsGranted
import com.jdrf.btdx.bluetooth.feature.BluetoothStateObserver
import com.jdrf.btdx.ui.MviBaseViewModel
import com.jdrf.btdx.ui.MviBaseViewState
import com.jdrf.btdx.ui.navigation.BtdxDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    bluetoothStateObserver: BluetoothStateObserver,
    private val bluetoothPermissionsObserver: BluetoothPermissionsObserver,
    bluetoothFeature: BluetoothFeature,
) : MviBaseViewModel<MainState, Any>(
    initialState = MainState(
        startDestination = startDestination(bluetoothFeature.isAvailable),
        bluetoothPermissionsGranted = bluetoothPermissionsObserver.currentState.allPermissionsGranted(),
        isBluetoothTurnedOn = bluetoothStateObserver.isBluetoothTurnedOn,
    )
) {

    init {
        bluetoothPermissionsObserver.permissionsStateFlow.bind {
            copy(bluetoothPermissionsGranted = it.allPermissionsGranted())
        }

        bluetoothStateObserver.bluetoothState.bind {
            copy(isBluetoothTurnedOn = it == BluetoothAdapter.STATE_ON)
        }
    }

    fun fetchPermissionsState() = viewModelScope.launch {
        bluetoothPermissionsObserver.updatePermissionsState()
    }

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
    val bluetoothPermissionsGranted: Boolean,
    val isBluetoothTurnedOn: Boolean,
) : MviBaseViewState
