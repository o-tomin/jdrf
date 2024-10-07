package com.jdrf.btdx.bluetooth.feature

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class BluetoothConnectionsManager @Inject constructor() {
    private val mutex = Mutex()
    private val _connections = MutableStateFlow<Map<String, DeviceConnectionObserver>>(emptyMap())

    suspend fun addConnection(macAddress: String, connection: DeviceConnectionObserver) {
        mutex.withLock {
            _connections.value = _connections.value.toMutableMap().apply {
                this[macAddress] = connection
            }
        }
    }

    suspend fun retrieveConnection(macAddress: String): DeviceConnectionObserver? {
        mutex.withLock {
            return _connections.value[macAddress]
        }
    }

    suspend fun contains(macAddress: String): Boolean {
        return mutex.withLock {
            _connections.value[macAddress] != null
        }
    }

    suspend fun deleteConnection(macAddress: String) {
        mutex.withLock {
            _connections.value = _connections.value.toMutableMap().apply {
                remove(macAddress)
            }
        }
    }
}