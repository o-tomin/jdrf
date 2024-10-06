package com.jdrf.btdx.ui.scanner

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jdrf.btdx.R
import com.jdrf.btdx.ext.eLog
import com.jdrf.btdx.ext.shToast

@SuppressLint("MissingPermission")
@Composable
fun DeviceScannerScreen(
    viewModel: DeviceScannerViewModel = hiltViewModel(),
    onDeviceDetails: (String) -> Unit,
    onBluetoothDisabled: @Composable () -> Unit,
) {
    val mviState by viewModel.state.collectAsStateWithLifecycle()
    EventsHandler(viewModel, onBluetoothDisabled = onBluetoothDisabled)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.background
            ),
        bottomBar = {
            Button(
                onClick = {
                    if (mviState.isScanning) viewModel.stopScanning() else viewModel.startScanning()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = if (mviState.isScanning)
                        stringResource(R.string.stop)
                    else
                        stringResource(R.string.scan),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Visible
                )
            }
        },
        content = { paddings ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddings),
                state = rememberLazyListState(),
            ) {
                items(mviState.devices.toList()) { btdxDevice ->
                    Text(
                        modifier = Modifier
                            .height(20.dp)
                            .clickable {
                                if (!btdxDevice.isConnected)
                                    viewModel.connect(btdxDevice)
                                else
                                    viewModel.disconnect(btdxDevice)
                            },
                        text = "${btdxDevice.device.name} ${btdxDevice.device.address} ${btdxDevice.isConnected}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    )
}

@Composable
fun EventsHandler(
    viewModel: DeviceScannerViewModel,
    context: Context = LocalContext.current,
    shToast: (String) -> Unit = context::shToast,
    onBluetoothDisabled: @Composable () -> Unit
) {
    when (val event = viewModel.events.collectAsStateWithLifecycle(DeviceScannerEvent.Idle).value) {
        is DeviceScannerEvent.Error -> eLog(event.t)
        DeviceScannerEvent.Idle -> {}
        DeviceScannerEvent.ShowEnableBluetoothScreen -> onBluetoothDisabled()
        else -> shToast("$event")
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light theme")
@Composable
fun DeviceScannerScreenPreview() =
    DeviceScannerScreen(onDeviceDetails = {}, onBluetoothDisabled = {})
