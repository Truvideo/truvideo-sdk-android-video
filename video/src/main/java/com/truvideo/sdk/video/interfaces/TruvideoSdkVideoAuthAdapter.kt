package com.truvideo.sdk.video.interfaces

import com.truvideo.sdk.video.model.TruvideoSdkVideoSdkFeature

internal interface TruvideoSdkVideoAuthAdapter {

    fun validateAuthentication()

    fun verifyFeatureAvailable(feature: TruvideoSdkVideoSdkFeature)
}