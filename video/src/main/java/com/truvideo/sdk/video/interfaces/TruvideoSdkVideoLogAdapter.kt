package com.truvideo.sdk.video.interfaces

import truvideo.sdk.common.model.TruvideoSdkLogSeverity

internal interface TruvideoSdkVideoLogAdapter {
    fun addLog(
        eventName: String,
        message: String,
        severity: TruvideoSdkLogSeverity
    )
}