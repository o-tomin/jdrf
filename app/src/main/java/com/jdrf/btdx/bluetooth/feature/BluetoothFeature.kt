package com.jdrf.btdx.bluetooth.feature

import com.jdrf.btdx.Feature
import com.jdrf.btdx.di.feature.FeatureName
import com.jdrf.btdx.di.feature.IsFeatureAvailable
import javax.inject.Inject

class BluetoothFeature @Inject constructor(
    @IsFeatureAvailable(FeatureName.BLUETOOTH) override val isAvailable: Boolean
) : Feature
