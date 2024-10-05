package com.jdrf.btdx.ui.permissions

import androidx.lifecycle.viewModelScope
import com.jdrf.btdx.bluetooth.feature.BluetoothPermissionsObserver
import com.jdrf.btdx.bluetooth.feature.BluetoothPermissionsObserver.Companion.allPermissionsGranted
import com.jdrf.btdx.ui.MviBaseViewModel
import com.jdrf.btdx.ui.MviBaseViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionsViewModel @Inject constructor(
    private val bluetoothPermissionsObserver: BluetoothPermissionsObserver
) : MviBaseViewModel<PermissionsState, Any>(
    initialState = PermissionsState(
        permissions = bluetoothPermissionsObserver.currentState,
        allPermissionsGranted = bluetoothPermissionsObserver.currentState.allPermissionsGranted()
    )
) {
    init {
        bluetoothPermissionsObserver.permissionsStateFlow.bind {
            copy(
                permissions = it,
                allPermissionsGranted = it.allPermissionsGranted()
            )
        }
    }

    fun fetchPermissionsState() = viewModelScope.launch {
        bluetoothPermissionsObserver.updatePermissionsState()
    }
}

data class PermissionsState(
    val permissions: Map<String, Boolean>,
    val allPermissionsGranted: Boolean,
) : MviBaseViewState
