package com.truvideo.sdk.video.interfaces

import com.truvideo.sdk.video.model.SdkFeature

internal interface TruvideoSdkVideoAuthAdapter {

    fun validateAuthentication()

    fun verifyFeatureAvailable(feature: SdkFeature)
}