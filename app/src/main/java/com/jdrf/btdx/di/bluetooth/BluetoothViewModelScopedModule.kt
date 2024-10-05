package com.jdrf.btdx.di.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService
import com.jdrf.btdx.bluetooth.feature.BluetoothPermissionsObserver.Companion.isPermissionGranted
import com.jdrf.btdx.ext.iLog
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object BluetoothViewModelScopedModule {

    @Provides
    @ViewModelScoped
    fun provideBluetoothAdapter(
        @ApplicationContext context: Context
    ): BluetoothAdapter {
        return context.getSystemService<BluetoothManager>()?.adapter.also {
            if (null == it) {
                iLog("If bluetooth adapter is null at this point, then it is system error. App crash is OK for now.")
            }
        }!!
    }

    @Provides
    @BluetoothPermissions
    fun provideBluetoothPermissions(
        @ApplicationContext context: Context
    ): Map<String, Boolean> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mapOf(
                Manifest.permission.BLUETOOTH_CONNECT to context.isPermissionGranted(Manifest.permission.BLUETOOTH_CONNECT),
                Manifest.permission.BLUETOOTH_SCAN to context.isPermissionGranted(Manifest.permission.BLUETOOTH_SCAN)
            )
        } else {
            mapOf(
                Manifest.permission.BLUETOOTH to context.isPermissionGranted(Manifest.permission.BLUETOOTH),
                Manifest.permission.BLUETOOTH_ADMIN to context.isPermissionGranted(Manifest.permission.BLUETOOTH_ADMIN)
            )
        }
    }
}
