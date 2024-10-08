package com.jdrf.btdx.ui.scanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothProfile
import androidx.lifecycle.viewModelScope
import com.jdrf.btdx.bluetooth.feature.BluetoothConnectionsManager
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
    private val connectionsManager: BluetoothConnectionsManager,
) : MviBaseViewModel<DeviceScannerState, DeviceScannerEvent>(
    DeviceScannerState(
        isScanning = false,
        devices = emptySet(),
        sortedWith = Comparators.NoOpComparator,
    )
) {
    init {
        with(deviceScannerObserver) {
            deviceFlow.bind { newDevice ->
                copy(
                    devices = if (devices.none { it.address == newDevice.address }) {
                        setOf(newDevice) + devices
                    } else devices
                )
            }
            errorCodesFlow.onEach {
                sendEvent(DeviceScannerEvent.ScanErrorCode(event = it))
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun connect(btdxDevice: DeviceScannerObserver.BtdxBluetoothDevice) = viewModelScope.launch {
        val connection = if (!connectionsManager.contains(btdxDevice.address)) {
            deviceConnectionObserverFactory.create(btdxDevice).apply {
                deviceResponseFlow.onEach { response ->
                    processDeviceResponse(response)
                }.launchIn(viewModelScope)
            }
        } else connectionsManager.retrieveConnection(btdxDevice.address)

        connection?.let {
            it.connectToRemoteDevice()
            connectionsManager.addConnection(btdxDevice.address, connection)
        }
    }

    fun disconnect(btdxDevice: DeviceScannerObserver.BtdxBluetoothDevice) = viewModelScope.launch {
        connectionsManager.retrieveConnection(btdxDevice.address)?.disconnectFromRemoteDevice()
        connectionsManager.deleteConnection(btdxDevice.address)
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
            is DeviceConnectionObserver.GattResponse.OnDescriptorRead,
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

    fun sortInAscendingOrderByName() {
        updateState { copy(sortedWith = Comparators.CompareByNameAscending) }
    }

    fun sortInAscendingOrderByMac() {
        updateState { copy(sortedWith = Comparators.CompareByMacAscending) }
    }

    fun sortByLastScannedOnTop() {
        updateState { copy(sortedWith = Comparators.NoOpComparator) }
    }

    sealed class Comparators {

        abstract val comparator: Comparator<DeviceScannerObserver.BtdxBluetoothDevice>

        data object CompareByNameAscending : Comparators() {
            override val comparator =
                compareBy<DeviceScannerObserver.BtdxBluetoothDevice> { it.name }
        }

        data object CompareByMacAscending : Comparators() {
            override val comparator =
                compareBy<DeviceScannerObserver.BtdxBluetoothDevice> { it.address }
        }

        data object NoOpComparator : Comparators() {
            override val comparator =
                Comparator<DeviceScannerObserver.BtdxBluetoothDevice> { _, _ -> 0 }
        }
    }
}

data class DeviceScannerState(
    val isScanning: Boolean,
    val devices: Set<DeviceScannerObserver.BtdxBluetoothDevice>,
    val sortedWith: DeviceScannerViewModel.Comparators
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
