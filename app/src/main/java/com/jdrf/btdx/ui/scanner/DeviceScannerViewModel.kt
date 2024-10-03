package com.jdrf.btdx.ui.scanner

import com.jdrf.btdx.ui.MviBaseViewModel
import com.jdrf.btdx.ui.MviBaseViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DeviceScannerViewModel @Inject constructor(
) : MviBaseViewModel<DeviceScannerState, DeviceScannerEvent>(
    DeviceScannerState(
        status = "view model connected"
    )
) {
}

data class DeviceScannerState(
    val status: String
) : MviBaseViewState

sealed class DeviceScannerEvent {
    data object Idle : DeviceScannerEvent()
    data class Error(val t: Throwable) : DeviceScannerEvent()
}