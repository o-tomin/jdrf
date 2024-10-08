package com.jdrf.btdx.ui.details

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import androidx.lifecycle.viewModelScope
import com.jdrf.btdx.bluetooth.feature.BluetoothConnectionsManager
import com.jdrf.btdx.bluetooth.feature.DeviceConnectionObserver
import com.jdrf.btdx.data.BtdxBluetoothService
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

                connection.discoverServices()
            }.onFailure {
                sendEvent(DeviceDetailsEvent.Error(it))
            }
        }
    }

    private suspend fun processDeviceResponse(response: DeviceConnectionObserver.GattResponse) {
        when (response) {
            is DeviceConnectionObserver.GattResponse.OnServicesDiscovered -> {
                viewModelScope.launch {
                    setState {
                        copy(
                            services = response.gatt.services.map(BtdxBluetoothService::create)
                        )
                    }
                }

            }

            is DeviceConnectionObserver.GattResponse.OnConnectionStateChange -> {
                if (response.newState != BluetoothProfile.STATE_CONNECTED) {
                    sendEvent(DeviceDetailsEvent.Disconnected)
                }
            }

            is DeviceConnectionObserver.GattResponse.OnCharacteristicRead -> {
                viewModelScope.launch {
                    with(state.value) {
                        val updatedServices = services.map { service ->
                            val updatedCharacteristics = service.characteristics.map {
                                if (it.uuid == response.characteristic.uuid) {
                                    it.copy(value = response.value.toByteArray().decodeToString())
                                } else it
                            }
                            service.copy(
                                characteristics = updatedCharacteristics
                            )
                        }

                        setState { copy(services = updatedServices) }
                    }
                }
            }

            is DeviceConnectionObserver.GattResponse.OnDescriptorRead -> {
                viewModelScope.launch {
                    with(state.value) {
                        val updatedServices = services.map { service ->
                            val updatedCharacteristics = service.characteristics.map {
                                if (it.uuid == response.descriptor.characteristic.uuid) {
                                    it.copy(
                                        descriptors = it.descriptors.map {
                                            if (it.uuid == response.descriptor.uuid) {
                                                it.copy(
                                                    value = response.value.toByteArray()
                                                        .decodeToString()
                                                )
                                            } else it
                                        }
                                    )
                                } else it
                            }
                            service.copy(
                                characteristics = updatedCharacteristics
                            )
                        }

                        setState { copy(services = updatedServices) }
                    }
                }

            }

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

    fun readCharacteristic(characteristic: BluetoothGattCharacteristic?) =
        runCatching {
            with(state.value) {
                connection?.readCharacteristic(characteristic!!)
            }
        }.onFailure {
            sendEvent(DeviceDetailsEvent.Error(it))
        }
}

data class DeviceDetailsState(
    val connection: DeviceConnectionObserver? = null,
    val services: List<BtdxBluetoothService>
) : MviBaseViewState

sealed class DeviceDetailsEvent {
    data object Idle : DeviceDetailsEvent()
    data object Disconnected : DeviceDetailsEvent()
    data class GattResponse(
        val response: DeviceConnectionObserver.GattResponse
    ) : DeviceDetailsEvent()

    data class Error(val t: Throwable) : DeviceDetailsEvent()
}