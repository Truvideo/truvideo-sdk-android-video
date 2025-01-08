package com.truvideo.sdk.video.adapters

import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoLogAdapter
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoVersionPropertiesAdapter
import truvideo.sdk.common.model.TruvideoSdkLog
import truvideo.sdk.common.model.TruvideoSdkLogModule
import truvideo.sdk.common.model.TruvideoSdkLogSeverity
import truvideo.sdk.common.sdk_common

internal class TruvideoSdkVideoLogAdapterImpl(
    versionPropertiesAdapter: TruvideoSdkVideoVersionPropertiesAdapter
) : TruvideoSdkVideoLogAdapter {

    private val moduleVersion = versionPropertiesAdapter.readProperty("versionName") ?: "Unknown"

    override fun addLog(eventName: String, message: String, severity: TruvideoSdkLogSeverity) {
        sdk_common.log.add(
            TruvideoSdkLog(
                tag = eventName,
                message = message,
                severity = severity,
                module = TruvideoSdkLogModule.VIDEO,
                moduleVersion = moduleVersion,
            )
        )
    }
}