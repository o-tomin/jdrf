package com.jdrf.btdx.ui.details

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import androidx.lifecycle.viewModelScope
import com.jdrf.btdx.bluetooth.feature.BluetoothConnectionsManager
import com.jdrf.btdx.bluetooth.feature.DeviceConnectionObserver
import com.jdrf.btdx.ui.MviBaseViewModel
import com.jdrf.btdx.ui.MviBaseViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceDetailsViewModel @Inject constructor(
    private val connectionsManager: BluetoothConnectionsManager,
) : MviBaseViewModel<DeviceDetailsState, DeviceDetailsEvent>(
    DeviceDetailsState(
        connection = null,
        services = emptyList(),
    )
) {

    @SuppressLint("MissingPermission")
    fun fetchConnection(macAddress: String?) = viewModelScope.launch {
        macAddress?.let {
            runCatching {
                connectionsManager.retrieveConnection(macAddress)!!
            }.onSuccess { connection ->
                connection.deviceResponseFlow.onEach { response ->
                    processDeviceResponse(response)
                }.launchIn(viewModelScope)

                setState { copy(connection = connection) }
            }.onFailure {
                sendEvent(DeviceDetailsEvent.Error(it))
            }
        }
    }

    fun discoverServices() {
        with(state.value) {
            connection?.discoverServices()
        }
    }

    private suspend fun processDeviceResponse(response: DeviceConnectionObserver.GattResponse) {
        when (response) {
            is DeviceConnectionObserver.GattResponse.OnServicesDiscovered -> {
                updateState {
                    copy(
                        services = services + response.gatt.services
                    )
                }
            }

            is DeviceConnectionObserver.GattResponse.OnConnectionStateChange -> {
                if (response.newState != BluetoothProfile.STATE_CONNECTED) {
                    sendEvent(DeviceDetailsEvent.ShowDeviceScannerScreen)
                }
            }

            is DeviceConnectionObserver.GattResponse.OnCharacteristicRead,
            is DeviceConnectionObserver.GattResponse.OnCharacteristicWrite,
            is DeviceConnectionObserver.GattResponse.OnMtuChanged -> {
                sendEvent(DeviceDetailsEvent.GattResponse(response))
            }
        }
    }

    fun readDescriptor(descriptor: BluetoothGattDescriptor?) {
        runCatching {
            with(state.value) {
                connection?.readDescriptor(descriptor!!)
            }
        }.onFailure {
            sendEvent(DeviceDetailsEvent.Error(it))
        }
    }
}

data class DeviceDetailsState(
    val connection: DeviceConnectionObserver? = null,
    val services: List<BluetoothGattService>
) : MviBaseViewState

sealed class DeviceDetailsEvent {
    data object Idle : DeviceDetailsEvent()
    data object ShowDeviceScannerScreen : DeviceDetailsEvent()
    data class GattResponse(
        val response: DeviceConnectionObserver.GattResponse
    ) : DeviceDetailsEvent()

    data class Error(val t: Throwable) : DeviceDetailsEvent()
}