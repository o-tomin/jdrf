package com.jdrf.btdx.bluetooth.feature

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.jdrf.btdx.di.bluetooth.BluetoothPermissions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class BluetoothPermissionsObserver @Inject constructor(
    @ApplicationContext private val context: Context,
    @BluetoothPermissions private val permissions: Map<String, Boolean>,
) {
    private val mutex = Mutex()
    private val _permissionsStateFlow = MutableStateFlow(permissions)
    val permissionsStateFlow: Flow<Map<String, Boolean>>
        get() = _permissionsStateFlow
    val currentState: Map<String, Boolean>
        get() = _permissionsStateFlow.value

    suspend fun updatePermissionsState() {
        mutex.withLock {
            _permissionsStateFlow.value = _permissionsStateFlow.value.mapValues {
                context.isPermissionGranted(it.key)
            }
        }
    }

    companion object {
        fun Map<String, Boolean>.allPermissionsGranted() = all { it.value }

        fun Context.isPermissionGranted(permission: String) = ContextCompat
            .checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    }
}
