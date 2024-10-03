package com.jdrf.btdx.ui.details

import com.jdrf.btdx.ui.MviBaseViewModel
import com.jdrf.btdx.ui.MviBaseViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DeviceDetailsViewModel @Inject constructor(
) : MviBaseViewModel<DeviceDetailsState, DeviceDetailsEvent>(
    DeviceDetailsState(
        status = "view model connected"
    )
) {
}

data class DeviceDetailsState(
    val status: String
) : MviBaseViewState

sealed class DeviceDetailsEvent {
    data object Idle : DeviceDetailsEvent()
    data class Error(val t: Throwable) : DeviceDetailsEvent()
}