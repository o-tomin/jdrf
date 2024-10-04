package com.jdrf.btdx.di.general

import android.content.Context
import android.content.pm.PackageManager
import com.jdrf.btdx.ext.iLog
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GeneralSingletonModule {

    @Provides
    @Singleton
    fun providePackageManager(@ApplicationContext context: Context): PackageManager {
        return context.packageManager.also {
            if (null == this) {
                iLog("The world is already broken, so app collapsing will not make it worsen")
            }
        }!!
    }
}
