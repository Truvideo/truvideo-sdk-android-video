package com.truvideo.sdk.video.adapters

import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoAuthAdapter
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoLogAdapter
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoVersionPropertiesAdapter
import com.truvideo.sdk.video.model.TruvideoSdkVideoSdkFeature
import truvideo.sdk.common.exceptions.TruvideoSdkAuthenticationRequiredException
import truvideo.sdk.common.exceptions.TruvideoSdkException
import truvideo.sdk.common.exceptions.TruvideoSdkNotInitializedException
import truvideo.sdk.common.model.TruvideoSdkLogSeverity
import truvideo.sdk.common.sdk_common
import truvideo.sdk.common.util.TruvideoSdkCommonExceptionParser
import truvideo.sdk.common.util.parse

internal class TruvideoSdkVideoAuthAdapterImpl(
    private val logAdapter: TruvideoSdkVideoLogAdapter,
    versionPropertiesAdapter: TruvideoSdkVideoVersionPropertiesAdapter
) : TruvideoSdkVideoAuthAdapter {

    private val validateAuthentication: Boolean = versionPropertiesAdapter.readProperty("validateAuthentication") != "false"

    private fun isAuthenticated(): Boolean {
        if (!validateAuthentication) return true

        try {
            return sdk_common.auth.isAuthenticated()
        } catch (exception: Exception) {
            val parsedException = TruvideoSdkCommonExceptionParser().parse(exception)
            parsedException.printStackTrace()

            logAdapter.addLog(
                eventName = "event_video_auth_validate_is_authenticated",
                message = "Validate is authenticated failed: ${parsedException.localizedMessage}",
                severity = TruvideoSdkLogSeverity.ERROR
            )

            throw exception
        }
    }

    private fun isInitialized(): Boolean {
        if (!validateAuthentication) return true

        try {
            return sdk_common.auth.isInitialized
        } catch (exception: Exception) {
            val parsedException = TruvideoSdkCommonExceptionParser().parse(exception)
            parsedException.printStackTrace()

            logAdapter.addLog(
                eventName = "event_video_auth_validate_is_initialized",
                message = "Validate is initialized failed: ${parsedException.localizedMessage}",
                severity = TruvideoSdkLogSeverity.ERROR
            )

            throw exception
        }
    }

    override fun validateAuthentication() {
        if (!validateAuthentication) return

        val isAuthenticated = isAuthenticated()
        if (!isAuthenticated) {
            logAdapter.addLog(
                eventName = "event_video_auth_validate",
                message = "Validate authentication failed: SDK not authenticated",
                severity = TruvideoSdkLogSeverity.ERROR
            )
            throw TruvideoSdkAuthenticationRequiredException()
        }

        val isInitialized = isInitialized()
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

        if (!validateAuthentication) return

        val settings = sdk_common.auth.getSettings() ?: throw TruvideoSdkException("No settings available")

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