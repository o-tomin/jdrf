package com.jdrf.btdx.ui.scanner

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DeviceScannerScreen(
    viewModel: DeviceScannerViewModel = hiltViewModel(),
    onDeviceDetails: (String) -> Unit,
) {
    val mviState = viewModel.state.collectAsStateWithLifecycle()
    EventsHandler(viewModel)

    Column(Modifier.background(color = MaterialTheme.colorScheme.background)) {
        Button(
            modifier = Modifier,
            onClick = { onDeviceDetails("MAC") }
        ) {
            Text(text = "DeviceScannerScreen")
        }
        Text(text = mviState.value.status)
    }
}

@Composable
fun EventsHandler(viewModel: DeviceScannerViewModel) {
    val event: DeviceScannerEvent by viewModel.events.collectAsStateWithLifecycle(DeviceScannerEvent.Idle)
    when (event) {
        is DeviceScannerEvent.Error -> TODO()
        DeviceScannerEvent.Idle -> {}
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light theme")
@Composable
fun DeviceScannerScreenPreview() = DeviceScannerScreen {}
