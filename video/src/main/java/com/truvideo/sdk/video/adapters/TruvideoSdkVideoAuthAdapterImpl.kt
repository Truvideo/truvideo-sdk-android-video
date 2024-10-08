package com.truvideo.sdk.video.adapters

import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoAuthAdapter
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoLogAdapter
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoVersionPropertiesAdapter
import com.truvideo.sdk.video.model.TruvideoSdkVideoSdkFeature
import truvideo.sdk.common.exception.TruvideoSdkAuthenticationRequiredException
import truvideo.sdk.common.exception.TruvideoSdkException
import truvideo.sdk.common.exception.TruvideoSdkNotInitializedException
import truvideo.sdk.common.model.TruvideoSdkLogSeverity
import truvideo.sdk.common.sdk_common

internal class TruvideoSdkVideoAuthAdapterImpl(
    private val logAdapter: TruvideoSdkVideoLogAdapter,
    versionPropertiesAdapter: TruvideoSdkVideoVersionPropertiesAdapter
) : TruvideoSdkVideoAuthAdapter {

    private val shouldValidate = versionPropertiesAdapter.readProperty("validateAuthentication") != "false"

    override fun validateAuthentication() {
        if (!shouldValidate) return

        val isAuthenticated = sdk_common.auth.isAuthenticated
        if (!isAuthenticated) {
            logAdapter.addLog(
                eventName = "event_video_auth_validate",
                message = "Validate authentication failed: SDK not authenticated",
                severity = TruvideoSdkLogSeverity.ERROR
            )
            throw TruvideoSdkAuthenticationRequiredException()
        }

        val isInitialized = sdk_common.auth.isInitialized
        if (!isInitialized) {
            logAdapter.addLog(
                eventName = "event_video_auth_validate",
                message = "Validate authentication failed: SDK not initialized",
                severity = TruvideoSdkLogSeverity.ERROR
            )
            throw TruvideoSdkNotInitializedException()
        }
    }

    override fun verifyFeatureAvailable(feature: TruvideoSdkVideoSdkFeature) {
        if (!shouldValidate) return

        val settings = sdk_common.auth.settings ?: throw TruvideoSdkException("No settings available")

        val isEnabled = when (feature) {
            TruvideoSdkVideoSdkFeature.NOISE_CANCELLING -> settings.noiseCancelling
        }

        if (!isEnabled) {
            logAdapter.addLog(
                eventName = "event_video_auth_validate",
                message = "${feature.name} feature not enabled",
                severity = TruvideoSdkLogSeverity.ERROR
            )
            throw TruvideoSdkException("${feature.name} feature not enabled")
        }
    }

}