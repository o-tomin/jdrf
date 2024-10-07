package com.jdrf.btdx.ui.scanner

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
        topBar = {
            ScrollableTabRow(viewModel)
        },
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
                item {
                    Spacer(Modifier.padding(top = 4.dp))
                }
                items(mviState.devices.sortedWith(mviState.sortedWith.comparator)) { btdxDevice ->
                    DeviceCard(
                        name = btdxDevice.name,
                        macAddress = btdxDevice.address,
                        isConnected = btdxDevice.isConnected,
                        onDeviceDetails = onDeviceDetails,
                        onBtConnect = {
                            if (!btdxDevice.isConnected) {
                                viewModel.connect(btdxDevice)
                            } else {
                                viewModel.disconnect(btdxDevice)
                            }
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun DeviceCard(
    modifier: Modifier = Modifier,
    name: String,
    macAddress: String,
    isConnected: Boolean,
    onDeviceDetails: (String) -> Unit,
    onBtConnect: () -> Unit
) {
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onDeviceDetails(macAddress) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row {
                Box(
                    modifier = Modifier,
                    contentAlignment = Alignment.CenterEnd
                ) {
                    IconButton(
                        onClick = onBtConnect,
                        modifier = Modifier
                    ) {
                        Icon(
                            painter = if (isConnected)
                                painterResource(R.drawable.bt_connected)
                            else
                                painterResource(R.drawable.bt_disconnected),
                            contentDescription = "",
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1.0f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier,
                    )
                    Text(
                        text = macAddress,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

        }
    }
}

@Composable
fun ScrollableTabRow(
    viewModel: DeviceScannerViewModel
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        context.getString(R.string.name_ascending_sort),
        context.getString(R.string.mac_ascending_sort),
        context.getString(R.string.last_scanned_on_top),
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 8.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> viewModel.sortInAscendingOrderByName()
            1 -> viewModel.sortInAscendingOrderByMac()
            2 -> viewModel.sortByLastScannedOnTop()
        }
    }
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
fun DeviceCardPreview() {
    var isConnected by remember { mutableStateOf(false) }
    DeviceCard(
        name = "WH-1234567",
        macAddress = "00:11:22:33:45",
        isConnected = isConnected,
        onDeviceDetails = {},
        onBtConnect = {
            isConnected = !isConnected
        }
    )
}