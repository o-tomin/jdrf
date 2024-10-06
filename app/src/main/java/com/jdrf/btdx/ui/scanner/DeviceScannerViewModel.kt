package com.jdrf.btdx.ui.scanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothProfile
import androidx.lifecycle.viewModelScope
import com.jdrf.btdx.bluetooth.feature.DeviceConnectionObserver
import com.jdrf.btdx.bluetooth.feature.DeviceConnectionObserverFactory
import com.jdrf.btdx.bluetooth.feature.DeviceScannerObserver
import com.jdrf.btdx.ui.MviBaseViewModel
import com.jdrf.btdx.ui.MviBaseViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceScannerViewModel @Inject constructor(
    private val deviceScannerObserver: DeviceScannerObserver,
    private val deviceConnectionObserverFactory: DeviceConnectionObserverFactory,
) : MviBaseViewModel<DeviceScannerState, DeviceScannerEvent>(
    DeviceScannerState(
        isScanning = false,
        devices = emptySet(),
        connections = emptyMap()
    )
) {
    init {
        with(deviceScannerObserver) {
            deviceFlow.bind { newDevice ->
                copy(devices = setOf(newDevice) + devices)
            }
            errorCodesFlow.onEach {
                sendEvent(DeviceScannerEvent.ScanErrorCode(event = it))
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun connect(btdxDevice: DeviceScannerObserver.BtdxBluetoothDevice) = viewModelScope.launch {
        with(state.value) {
            val connection = if (!connections.contains(btdxDevice.address)) {
                deviceConnectionObserverFactory.create(btdxDevice).apply {
                    deviceResponseFlow.onEach { response ->
                        processDeviceResponse(response)
                    }.launchIn(viewModelScope)
                }
            } else connections[btdxDevice.address]

            connection?.let {
                it.connectToRemoteDevice()
                setState {
                    copy(connections = mapOf(btdxDevice.address to connection) + connections)
                }
            }
        }
    }

    fun disconnect(btdxDevice: DeviceScannerObserver.BtdxBluetoothDevice) = viewModelScope.launch {
        with(state.value) {
            connections[btdxDevice.address]?.disconnectFromRemoteDevice()
            setState {
                copy(
                    connections = connections.filterNot { it.key == btdxDevice.address }
                )
            }
        }
    }

    private suspend fun processDeviceResponse(response: DeviceConnectionObserver.GattResponse) {
        when (response) {
            is DeviceConnectionObserver.GattResponse.OnConnectionStateChange -> {
                val isConnected: Boolean = response.newState == BluetoothProfile.STATE_CONNECTED
                setState {
                    copy(
                        devices = devices.map { btdxDevice ->
                            if (btdxDevice.isSameDevice(response.gatt.device)) {
                                btdxDevice.copy(
                                    isConnected = isConnected
                                )
                            } else btdxDevice
                        }.toSet()
                    )
                }
                sendEvent(
                    DeviceScannerEvent.GattConnectionResponse(
                        isConnected = isConnected,
                        address = response.gatt.device?.address ?: ""
                    )
                )
            }

            is DeviceConnectionObserver.GattResponse.OnCharacteristicRead,
            is DeviceConnectionObserver.GattResponse.OnCharacteristicWrite,
            is DeviceConnectionObserver.GattResponse.OnMtuChanged,
            is DeviceConnectionObserver.GattResponse.OnServicesDiscovered -> {
                sendEvent(DeviceScannerEvent.GattResponse(response))
            }
        }
    }

    fun stopScanning() {
        updateState { copy(isScanning = false) }
        deviceScannerObserver.stopScanning()
    }

    fun startScanning() {
        updateState { copy(isScanning = true) }
        deviceScannerObserver.startScanning()
    }
}

data class DeviceScannerState(
    val isScanning: Boolean,
    val devices: Set<DeviceScannerObserver.BtdxBluetoothDevice>,
    val connections: Map<String, DeviceConnectionObserver>
) : MviBaseViewState

sealed class DeviceScannerEvent {
    data object Idle : DeviceScannerEvent()
    data object ShowEnableBluetoothScreen : DeviceScannerEvent()

    data class ScanErrorCode(
        val event: DeviceScannerObserver.BtdxErrorCode
    ) : DeviceScannerEvent()

    data class GattResponse(
        val response: DeviceConnectionObserver.GattResponse
    ) : DeviceScannerEvent()

    data class GattConnectionResponse(
        val isConnected: Boolean,
        val address: String,
    ) : DeviceScannerEvent()

    data class Error(
        val t: Throwable
    ) : DeviceScannerEvent()
}
