package com.jdrf.btdx.ui.details

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jdrf.btdx.R
import com.jdrf.btdx.data.BtdxBluetoothService
import com.jdrf.btdx.ext.eLog
import com.jdrf.btdx.ext.shToast
import kotlinx.coroutines.CoroutineScope

@Composable
fun DeviceDetailsScreen(
    macAddress: String?,
    viewModel: DeviceDetailsViewModel = hiltViewModel<DeviceDetailsViewModel>().also {
        it.fetchConnection(macAddress)
    },
) {
    val mviState by viewModel.state.collectAsStateWithLifecycle()
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
                    services = mviState.services,
                    parent = null,
                    viewModel = viewModel,
                    viewScope = rememberCoroutineScope()
                )
            }
        }
    )
}

@Composable
fun BluetoothGattServiceView(
    services: List<BtdxBluetoothService>,
    parent: BtdxBluetoothService?,
    viewModel: DeviceDetailsViewModel,
    viewScope: CoroutineScope
) {
    services.forEach { service ->
        DisplayBluetoothGattService(
            service,
            parent,
            viewModel,
            viewScope,
        )
    }
}

@Composable
fun DisplayBluetoothGattService(
    service: BtdxBluetoothService,
    parent: BtdxBluetoothService?,
    viewModel: DeviceDetailsViewModel,
    viewScope: CoroutineScope,
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .border(1.dp, MaterialTheme.colorScheme.background)
            .padding(8.dp)
    ) {
        Text(
            text = stringResource(R.string.service_uuid).format(service.uuid),
            style = MaterialTheme.typography.titleLarge
        )
        if (parent != null) {
            Text(
                text = stringResource(R.string.parent_service_uuid).format(parent.uuid),
                style = MaterialTheme.typography.titleMedium
            )
        }
        Text(
            text = stringResource(R.string.service_type).format(service.type::class.simpleName),
            style = MaterialTheme.typography.titleMedium
        )

        service.characteristics.forEach { characteristic ->
            Spacer(Modifier.height(5.dp))
            Text(
                text = stringResource(R.string.characteristic_uuid).format(characteristic.uuid),
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = stringResource(R.string.permissions).format(characteristic.btdxPermissions.joinToString()),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = stringResource(R.string.properties).format(characteristic.properties.joinToString()),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = stringResource(R.string.write_type).format(characteristic.writeType),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                modifier = Modifier.clickable {
                    viewModel.readCharacteristic(characteristic.bluetoothGattCharacteristic)
                },
                text = stringResource(R.string.value).format(characteristic.value),
                style = MaterialTheme.typography.bodySmall
            )
            characteristic.descriptors.forEach { descriptor ->
                Column(Modifier.padding(start = 5.dp)) {
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = stringResource(R.string.descriptor_uuid).format(descriptor.uuid),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = stringResource(R.string.permissions).format(descriptor.permissions.joinToString()),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        modifier = Modifier.clickable { viewModel.readDescriptor(descriptor.bluetoothGattDescriptor) },
                        text = stringResource(R.string.value).format(descriptor.value),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }


        val nestedServices = service.includedServices
        if (nestedServices.isNotEmpty()) {
            Text(text = stringResource(R.string.included_services))
            BluetoothGattServiceView(
                nestedServices,
                service,
                viewModel = viewModel,
                rememberCoroutineScope()
            )
        }
    }
}

@Composable
fun EventsHandler(viewModel: DeviceDetailsViewModel) {
    when (val event = viewModel.events.collectAsStateWithLifecycle(DeviceDetailsEvent.Idle).value) {
        is DeviceDetailsEvent.Error -> eLog(event.t)
        DeviceDetailsEvent.Idle -> {}
        is DeviceDetailsEvent.GattResponse,
        DeviceDetailsEvent.Disconnected -> {
            LocalContext.current.shToast("$event")
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light theme")
@Composable
fun DeviceDetailsScreenPreview() = DeviceDetailsScreen(macAddress = "MAC")
