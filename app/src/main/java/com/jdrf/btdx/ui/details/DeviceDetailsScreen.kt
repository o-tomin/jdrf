package com.jdrf.btdx.ui.details

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DeviceDetailsScreen(
    viewModel: DeviceDetailsViewModel = hiltViewModel(),
    macAddress: String?,
) {
    val mviState = viewModel.state.collectAsStateWithLifecycle()
    EventsHandler(viewModel)

    Column(Modifier.background(color = MaterialTheme.colorScheme.background)) {
        Text(text = "DeviceDetailsScreen(macAddress=$macAddress)")
        Text(text = mviState.value.status)
    }
}

@Composable
fun EventsHandler(viewModel: DeviceDetailsViewModel) {
    val event by viewModel.events.collectAsStateWithLifecycle(DeviceDetailsEvent.Idle)

    when (event) {
        is DeviceDetailsEvent.Error -> TODO()
        DeviceDetailsEvent.Idle -> {}
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light theme")
@Composable
fun DeviceDetailsScreenPreview() = DeviceDetailsScreen(macAddress = "MAC")
