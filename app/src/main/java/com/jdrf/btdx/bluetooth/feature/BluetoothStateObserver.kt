package com.jdrf.btdx.bluetooth.feature

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED
import android.bluetooth.BluetoothAdapter.EXTRA_STATE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import com.jdrf.btdx.di.coroutines.BtdxDispatchers
import com.jdrf.btdx.di.coroutines.Dispatcher
import com.jdrf.btdx.di.general.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

@ViewModelScoped
class BluetoothStateObserver @Inject constructor(
    @ApplicationContext context: Context,
    @ApplicationScope appScope: CoroutineScope,
    @Dispatcher(BtdxDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val adapter: BluetoothAdapter,
) {

    val isBluetoothTurnedOn: Boolean
        get() = adapter.state == BluetoothAdapter.STATE_ON

    val bluetoothState: SharedFlow<Int> =
        callbackFlow {
            trySend(adapter.state)

            val bluetoothReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action == ACTION_STATE_CHANGED) {
                        val state = intent.getIntExtra(EXTRA_STATE, BluetoothAdapter.STATE_OFF)
                        trySend(state)
                    }
                }
            }

            ContextCompat.registerReceiver(
                context,
                bluetoothReceiver,
                IntentFilter(ACTION_STATE_CHANGED),
                ContextCompat.RECEIVER_EXPORTED
            )

            awaitClose {
                context.unregisterReceiver(bluetoothReceiver)
            }
        }
            .distinctUntilChanged()
            .conflate()
            .flowOn(ioDispatcher)
            // Sharing the callback to prevent multiple BroadcastReceivers being registered
            .shareIn(appScope, SharingStarted.WhileSubscribed(5_000), 1)
}
