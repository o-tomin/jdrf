package com.jdrf.btdx.bluetooth.feature

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGatt.GATT_CONNECTION_CONGESTED
import android.bluetooth.BluetoothGatt.GATT_FAILURE
import android.bluetooth.BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION
import android.bluetooth.BluetoothGatt.GATT_INSUFFICIENT_AUTHORIZATION
import android.bluetooth.BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION
import android.bluetooth.BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH
import android.bluetooth.BluetoothGatt.GATT_INVALID_OFFSET
import android.bluetooth.BluetoothGatt.GATT_READ_NOT_PERMITTED
import android.bluetooth.BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.bluetooth.BluetoothGatt.GATT_WRITE_NOT_PERMITTED
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.conflate

class DeviceConnectionObserver(
    @ApplicationContext private val context: Context,
    private val btdxDevice: DeviceScannerObserver.BtdxBluetoothDevice,
) : BluetoothGattCallback() {

    private val _deviceResponseFlow = MutableSharedFlow<GattResponse>(
        replay = 0, extraBufferCapacity = 5, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val deviceResponseFlow: Flow<GattResponse>
        get() = _deviceResponseFlow.conflate()

    private val bluetoothGattCallback: BluetoothGattCallback by lazy {
        object : BluetoothGattCallback() {
            override fun onConnectionStateChange(
                gatt: BluetoothGatt,
                status: Int,
                newState: Int,
            ) {
                super.onConnectionStateChange(gatt, status, newState)
                _deviceResponseFlow.tryEmit(
                    GattResponse.OnConnectionStateChange(
                        gatt,
                        newState,
                        toGattStatus(status)
                    )
                )
            }

            override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
                super.onMtuChanged(gatt, mtu, status)
                _deviceResponseFlow.tryEmit(
                    GattResponse.OnMtuChanged(
                        gatt,
                        mtu,
                        toGattStatus(status),
                    )
                )
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                super.onServicesDiscovered(gatt, status)
                _deviceResponseFlow.tryEmit(
                    GattResponse.OnServicesDiscovered(
                        gatt,
                        toGattStatus(status),
                    )
                )
            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int,
            ) {
                super.onCharacteristicWrite(gatt, characteristic, status)
                _deviceResponseFlow.tryEmit(
                    GattResponse.OnCharacteristicWrite(
                        gatt,
                        characteristic,
                        toGattStatus(status)
                    )
                )
            }

            @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int,
            ) {
                super.onCharacteristicRead(gatt, characteristic, status)
                _deviceResponseFlow.tryEmit(
                    GattResponse.OnCharacteristicRead(
                        gatt,
                        characteristic,
                        toGattStatus(status)
                    )
                )
            }
        }

    }

    private lateinit var gatt: BluetoothGatt

    @SuppressLint("MissingPermission")
    fun connectToRemoteDevice() {
        gatt = btdxDevice.connectGatt(context, false, bluetoothGattCallback)
    }

    @SuppressLint("MissingPermission")
    fun discoverServices() {
        gatt.discoverServices()
    }

    @SuppressLint("MissingPermission")
    fun disconnectFromRemoteDevice() {
        if (::gatt.isInitialized) {
            gatt.disconnect()
            gatt.close()

            _deviceResponseFlow.tryEmit(
                GattResponse.OnConnectionStateChange(
                    gatt,
                    newState = BluetoothProfile.STATE_DISCONNECTING,
                    gattStatus = GattStatus.GattSuccess
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun toGattStatus(status: Int): GattStatus {
        return when (status) {
            GATT_SUCCESS -> GattStatus.GattSuccess
            GATT_READ_NOT_PERMITTED -> GattStatus.GattReadNotPermitted
            GATT_WRITE_NOT_PERMITTED -> GattStatus.GattWWriteNotPermitted
            GATT_INSUFFICIENT_AUTHENTICATION -> GattStatus.GattInsufficientAuthentication
            GATT_REQUEST_NOT_SUPPORTED -> GattStatus.GattRequestNotSupported
            GATT_INSUFFICIENT_ENCRYPTION -> GattStatus.GattInsufficientEncryption
            GATT_INVALID_OFFSET -> GattStatus.GattInvalidOffset
            GATT_INSUFFICIENT_AUTHORIZATION -> GattStatus.GattInsufficientAuthorization
            GATT_INVALID_ATTRIBUTE_LENGTH -> GattStatus.GattInvalidAttributeLength
            GATT_CONNECTION_CONGESTED -> GattStatus.GattConnectionCongested
            GATT_FAILURE -> GattStatus.GattFailure
            else -> GattStatus.GattUnknown
        }
    }

    @SuppressLint("MissingPermission")
    fun readDescriptor(descriptor: BluetoothGattDescriptor) {
        gatt.readDescriptor(descriptor)
    }

    sealed class GattResponse {
        data class OnConnectionStateChange(
            val gatt: BluetoothGatt,
            val newState: Int,
            val gattStatus: GattStatus,
        ) : GattResponse()

        data class OnMtuChanged(
            val gatt: BluetoothGatt,
            val mtu: Int,
            val gattStatus: GattStatus,
        ) : GattResponse()

        data class OnServicesDiscovered(
            val gatt: BluetoothGatt,
            val gattStatus: GattStatus,
        ) : GattResponse()

        data class OnCharacteristicWrite(
            val gatt: BluetoothGatt?,
            val characteristic: BluetoothGattCharacteristic?,
            val gattStatus: GattStatus,
        ) : GattResponse()

        data class OnCharacteristicRead(
            val gatt: BluetoothGatt,
            val characteristic: BluetoothGattCharacteristic,
            val gattStatus: GattStatus,
        ) : GattResponse()
    }

    sealed class GattStatus {
        data object GattSuccess : GattStatus()
        data object GattReadNotPermitted : GattStatus()
        data object GattWWriteNotPermitted : GattStatus()
        data object GattInsufficientAuthentication : GattStatus()
        data object GattRequestNotSupported : GattStatus()
        data object GattInsufficientEncryption : GattStatus()
        data object GattInvalidOffset : GattStatus()
        data object GattInsufficientAuthorization : GattStatus()
        data object GattInvalidAttributeLength : GattStatus()
        data object GattConnectionCongested : GattStatus()
        data object GattFailure : GattStatus()
        data object GattUnknown : GattStatus()
    }
}

class DeviceConnectionObserverFactory(
    @ApplicationContext private val context: Context,
) {
    fun create(btdxDevice: DeviceScannerObserver.BtdxBluetoothDevice): DeviceConnectionObserver {
        return DeviceConnectionObserver(
            context,
            btdxDevice
        )
    }
}
