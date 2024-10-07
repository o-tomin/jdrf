package com.jdrf.btdx.di.bluetooth

import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.FEATURE_BLUETOOTH
import android.content.pm.PackageManager.FEATURE_BLUETOOTH_LE
import androidx.core.content.getSystemService
import com.jdrf.btdx.bluetooth.feature.BluetoothConnectionsManager
import com.jdrf.btdx.di.feature.FeatureName
import com.jdrf.btdx.di.feature.IsFeatureAvailable
import com.jdrf.btdx.ext.iLog
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BluetoothSingletonScopedModule {

    @Provides
    @Singleton
    @IsFeatureAvailable(FeatureName.BLUETOOTH)
    fun hasSystemFeature(
        @ApplicationContext context: Context,
        packageManager: PackageManager,
    ): Boolean {
        val bluetoothAdapter = context.getSystemService<BluetoothManager>()?.adapter
        iLog("bluetoothAdapter is null=${bluetoothAdapter == null}")

        val hasBT = packageManager.hasSystemFeature(FEATURE_BLUETOOTH)
        iLog("hasBT=$hasBT")

        val hasBLE = packageManager.hasSystemFeature(FEATURE_BLUETOOTH_LE)
        iLog("hasBLE=$hasBLE")

        return bluetoothAdapter != null && hasBT && hasBLE
    }

    @Singleton
    @Provides
    fun provideConnectionsManager(): BluetoothConnectionsManager {
        return BluetoothConnectionsManager()
    }
}
