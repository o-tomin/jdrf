package com.jdrf.btdx.bluetooth.feature

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import com.jdrf.btdx.di.coroutines.BtdxDispatchers
import com.jdrf.btdx.di.coroutines.Dispatcher
import com.jdrf.btdx.di.general.ApplicationScope
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

@ViewModelScoped
class DeviceScannerObserver @Inject constructor(
    private val scannerSettings: ScanSettings?,
    private val bluetoothLeScanner: BluetoothLeScanner?,
    @ApplicationScope appScope: CoroutineScope,
    @Dispatcher(BtdxDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) {
    private val _errorCodesFlow = Channel<BtdxErrorCode>(Channel.UNLIMITED)
    val errorCodesFlow: Flow<BtdxErrorCode>
        get() = _errorCodesFlow.receiveAsFlow()

    private lateinit var scanCallback: ScanCallback

    val deviceFlow: SharedFlow<BtdxBluetoothDevice> =
        callbackFlow {
            scanCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    super.onScanResult(callbackType, result)
                    result?.device?.let { device ->
                        trySend(BtdxBluetoothDevice(device = device))
                    }
                }

                @SuppressLint("SwitchIntDef")
                override fun onScanFailed(errorCode: Int) {
                    super.onScanFailed(errorCode)

                    when (errorCode) {
                        SCAN_FAILED_ALREADY_STARTED ->
                            _errorCodesFlow.trySend(BtdxErrorCode.ScanFailedAlreadyStarted)

                        SCAN_FAILED_APPLICATION_REGISTRATION_FAILED ->
                            _errorCodesFlow.trySend(BtdxErrorCode.ScanFailedApplicationRegistrationFailed)

                        SCAN_FAILED_INTERNAL_ERROR ->
                            _errorCodesFlow.trySend(BtdxErrorCode.ScanFailedInternalError)

                        SCAN_FAILED_FEATURE_UNSUPPORTED ->
                            _errorCodesFlow.trySend(BtdxErrorCode.ScanFailedFeatureUnsupported)

                        SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES ->
                            _errorCodesFlow.trySend(BtdxErrorCode.ScanFailedOutOfHardwareResources)

                        SCAN_FAILED_SCANNING_TOO_FREQUENTLY ->
                            _errorCodesFlow.trySend(BtdxErrorCode.ScanFailedScanningTooFrequently)

                        SCAN_RESULT_NO_ERROR ->
                            _errorCodesFlow.trySend(BtdxErrorCode.ScanResultNoError)

                        else ->
                            _errorCodesFlow.trySend(BtdxErrorCode.ScanResultUnknown(errorCode))
                    }
                }
            }

            awaitClose {
                stopScanning()
            }
        }
            .distinctUntilChanged()
            .conflate()
            .flowOn(ioDispatcher)
            // Sharing the callback to prevent multiple BroadcastReceivers being registered
            .shareIn(appScope, SharingStarted.WhileSubscribed(5_000), 1)

    @SuppressLint("MissingPermission")
    fun startScanning() =
        bluetoothLeScanner?.startScan(
            null,
            scannerSettings,
            scanCallback,
        )

    @SuppressLint("MissingPermission")
    fun stopScanning() =
        bluetoothLeScanner?.stopScan(scanCallback)

    companion object {
        const val SCAN_RESULT_NO_ERROR = 0
    }

    data class BtdxBluetoothDevice(
        val device: BluetoothDevice,
        val isConnected: Boolean = false,
    )

    sealed class BtdxErrorCode {
        data object ScanFailedAlreadyStarted : BtdxErrorCode()
        data object ScanFailedApplicationRegistrationFailed : BtdxErrorCode()
        data object ScanFailedInternalError : BtdxErrorCode()
        data object ScanFailedFeatureUnsupported : BtdxErrorCode()
        data object ScanFailedOutOfHardwareResources : BtdxErrorCode()
        data object ScanFailedScanningTooFrequently : BtdxErrorCode()
        data object ScanResultNoError : BtdxErrorCode()
        data class ScanResultUnknown(val errorCode: Int) : BtdxErrorCode()
    }
}
