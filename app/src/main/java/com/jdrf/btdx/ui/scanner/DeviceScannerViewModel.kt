package com.jdrf.btdx.ui.scanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import com.jdrf.btdx.ui.MviBaseViewModel
import com.jdrf.btdx.ui.MviBaseViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DeviceScannerViewModel @Inject constructor(
    private val scannerSettings: ScanSettings?,
    private val bluetoothLeScanner: BluetoothLeScanner?,
) : MviBaseViewModel<DeviceScannerState, DeviceScannerEvent>(
    DeviceScannerState(
        isScanning = false,
        devices = emptySet()
    )
) {

    private val scanCallback: ScanCallback by lazy {
        object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                result?.device?.let { device ->
                    updateState { copy(devices = setOf(device) + devices) }
                }
            }

            @SuppressLint("SwitchIntDef")
            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)

                when (errorCode) {
                    SCAN_FAILED_ALREADY_STARTED -> sendEvent(DeviceScannerEvent.ScanFailedAlreadyStarted)
                    SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> sendEvent(DeviceScannerEvent.ScanFailedApplicationRegistrationFailed)
                    SCAN_FAILED_INTERNAL_ERROR -> sendEvent(DeviceScannerEvent.ScanFailedInternalError)
                    SCAN_FAILED_FEATURE_UNSUPPORTED -> sendEvent(DeviceScannerEvent.ScanFailedFeatureUnsupported)
                    SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES -> sendEvent(DeviceScannerEvent.ScanFailedOutOfHardwareResources)
                    SCAN_FAILED_SCANNING_TOO_FREQUENTLY -> sendEvent(DeviceScannerEvent.ScanFailedScanningTooFrequently)
                    SCAN_RESULT_NO_ERROR -> sendEvent(DeviceScannerEvent.ScanResultNoError)
                    else -> sendEvent(DeviceScannerEvent.ScanResultUnknown(errorCode))
                }
            }
        }
    }

    init {
        if (null == bluetoothLeScanner) {
            sendEvent(DeviceScannerEvent.ShowEnableBluetoothScreen)
        }
    }

    @SuppressLint("MissingPermission")
    fun startScanning() =
        bluetoothLeScanner?.startScan(
            null,
            scannerSettings,
            scanCallback,
        ).also {
            updateState { copy(isScanning = true) }
        }

    @SuppressLint("MissingPermission")
    fun stopScanning() =
        bluetoothLeScanner?.stopScan(scanCallback).also {
            updateState { copy(isScanning = false) }
        }

    companion object {
        const val SCAN_RESULT_NO_ERROR = 0
    }
}

data class DeviceScannerState(
    val isScanning: Boolean,
    val devices: Set<BluetoothDevice>,
) : MviBaseViewState

sealed class DeviceScannerEvent {
    data object Idle : DeviceScannerEvent()
    data object ShowEnableBluetoothScreen : DeviceScannerEvent()

    data object ScanFailedAlreadyStarted : DeviceScannerEvent()
    data object ScanFailedApplicationRegistrationFailed : DeviceScannerEvent()
    data object ScanFailedInternalError : DeviceScannerEvent()
    data object ScanFailedFeatureUnsupported : DeviceScannerEvent()
    data object ScanFailedOutOfHardwareResources : DeviceScannerEvent()
    data object ScanFailedScanningTooFrequently : DeviceScannerEvent()
    data object ScanResultNoError : DeviceScannerEvent()
    data class ScanResultUnknown(val errorCode: Int) : DeviceScannerEvent()

    data class Error(val t: Throwable) : DeviceScannerEvent()
}