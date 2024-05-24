package com.truvideo.sdk.video.adapters

import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoAuthAdapter
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoVersionPropertiesAdapter
import com.truvideo.sdk.video.model.SdkFeature
import com.truvideo.sdk.video.model.TruvideoSdkVideoException
import truvideo.sdk.common.exception.TruvideoSdkAuthenticationRequiredException
import truvideo.sdk.common.exception.TruvideoSdkNotInitializedException
import truvideo.sdk.common.sdk_common

internal class TruvideoSdkVideoAuthAdapterImpl(
    private val versionPropertiesAdapter: TruvideoSdkVideoVersionPropertiesAdapter
) : TruvideoSdkVideoAuthAdapter {

    private fun shouldValidate(): Boolean {
        return versionPropertiesAdapter.readProperty("validateAuthentication") != "false"
    }

    override fun validateAuthentication() {
        if (!shouldValidate()) return

        val isAuthenticated = sdk_common.auth.isAuthenticated.value
        if (!isAuthenticated) {
            throw TruvideoSdkAuthenticationRequiredException()
        }

        val isInitialized = sdk_common.auth.isInitialized.value
        if (!isInitialized) {
            throw TruvideoSdkNotInitializedException()
        }
    }

    override fun verifyFeatureAvailable(feature: SdkFeature) {
        val settings = sdk_common.auth.settings.value ?: throw TruvideoSdkVideoException("No settings available")

        val isEnabled = when (feature) {
            SdkFeature.NOISE_CANCELLING -> settings.noiseCancelling
        }

        if (!isEnabled) {
            throw TruvideoSdkVideoException("${feature.name} feature not enabled")
        }
    }

}