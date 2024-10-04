package com.jdrf.btdx.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.jdrf.btdx.bluetooth.feature.BluetoothFeature
import com.jdrf.btdx.ui.navigation.BtdxDestinations
import com.jdrf.btdx.ui.theme.BtdxTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var bluetoothFeature: BluetoothFeature

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BtdxTheme {
                val isBluetoothAvailable by remember {
                    mutableStateOf(bluetoothFeature.isAvailable)
                }

                BtdxApp(
                    startDestination = if (isBluetoothAvailable) {
                        BtdxDestinations.SCANNED_DEVICES_ROUTE
                    } else {
                        BtdxDestinations.APPLICATION_NOT_SUPPORTED_ROUTE
                    }
                )
            }
        }
    }

}
