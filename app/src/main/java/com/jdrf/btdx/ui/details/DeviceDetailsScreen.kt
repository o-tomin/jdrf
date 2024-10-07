package com.jdrf.btdx.ui.details

import android.bluetooth.BluetoothGattService
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jdrf.btdx.R
import com.jdrf.btdx.ext.eLog
import com.jdrf.btdx.ext.shToast

@Composable
fun DeviceDetailsScreen(
    macAddress: String?,
    viewModel: DeviceDetailsViewModel = hiltViewModel<DeviceDetailsViewModel>().also {
        it.fetchConnection(macAddress)
    },
) {
    val mviState by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(mviState.connection) {
        viewModel.discoverServices()
    }

    EventsHandler(viewModel)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.background
            ),
        content = { paddings ->
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(paddings),
            ) {
                BluetoothGattServiceView(
                    mviState.services,
                    viewModel = viewModel,
                )
            }
        }
    )
}

@Composable
fun BluetoothGattServiceView(
    services: List<BluetoothGattService>,
    parent: BluetoothGattService? = null,
    viewModel: DeviceDetailsViewModel
) {
    services.forEach { service ->
        DisplayBluetoothGattService(
            service,
            parent,
            viewModel
        )
    }
}

@Composable
fun DisplayBluetoothGattService(
    service: BluetoothGattService,
    parent: BluetoothGattService?,
    viewModel: DeviceDetailsViewModel,
) {
    val uuid = service.uuid.toString()
    val characteristics = service.characteristics

    Column(
        modifier = Modifier
            .padding(8.dp)
            .border(1.dp, MaterialTheme.colorScheme.background)
            .padding(8.dp)
    ) {
        Text(
            text = stringResource(R.string.service_uuid).format(uuid),
            style = MaterialTheme.typography.titleLarge
        )
        if (parent != null) {
            Text(
                text = stringResource(R.string.parent_service_uuid).format(parent.uuid),
                style = MaterialTheme.typography.titleMedium
            )
        }
        Text(
            text = stringResource(R.string.characteristics_count).format(characteristics.size),
            style = MaterialTheme.typography.titleMedium
        )

        characteristics.forEach { characteristic ->
            Text(
                text = stringResource(R.string.characteristic_uuid).format(characteristic.uuid),
                style = MaterialTheme.typography.bodyMedium
            )
            characteristic.descriptors.forEach { descriptor ->
                Column(Modifier.padding(start = 5.dp)) {
                    Text(
                        text = "Descriptor UUID: ${descriptor.uuid}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Descriptor Permissions: ${descriptor.permissions}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    key(descriptor.value) {
                        Text(
                            text = "Descriptor Value: ${descriptor.value}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        LaunchedEffect(descriptor) {
                            viewModel.readDescriptor(descriptor)
                        }
                    }
                }

            }
        }

        val nestedServices = service.includedServices
        if (nestedServices != null && nestedServices.isNotEmpty()) {
            Text(text = stringResource(R.string.included_services))
            BluetoothGattServiceView(nestedServices, viewModel = viewModel)
        }
    }
}

@Composable
fun EventsHandler(viewModel: DeviceDetailsViewModel) {
    when (val event = viewModel.events.collectAsStateWithLifecycle(DeviceDetailsEvent.Idle).value) {
        is DeviceDetailsEvent.Error -> eLog(event.t)
        DeviceDetailsEvent.Idle -> {}
        is DeviceDetailsEvent.GattResponse,
        DeviceDetailsEvent.ShowDeviceScannerScreen -> {
            LocalContext.current.shToast("$event")
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light theme")
@Composable
fun DeviceDetailsScreenPreview() = DeviceDetailsScreen(macAddress = "MAC")
