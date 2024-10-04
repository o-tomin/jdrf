package com.jdrf.btdx.di.feature

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IsFeatureAvailable(
    val featureName: FeatureName,
)
