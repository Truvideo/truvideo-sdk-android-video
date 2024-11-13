package com.truvideo.sdk.video.model

import kotlinx.serialization.Serializable

@Serializable
enum class TruvideoSdkVideoRotation {
    DEGREES_0,
    DEGREES_90,
    DEGREES_180,
    DEGREES_270,
}

val TruvideoSdkVideoRotation.ffmpegDegrees: Int
    get() {
        return when (this) {
            TruvideoSdkVideoRotation.DEGREES_0 -> 0
            TruvideoSdkVideoRotation.DEGREES_90 -> -90
            TruvideoSdkVideoRotation.DEGREES_180 -> 180
            TruvideoSdkVideoRotation.DEGREES_270 -> 90
        }
    }

val TruvideoSdkVideoRotation.degrees: Int
    get() {
        return when (this) {
            TruvideoSdkVideoRotation.DEGREES_0 -> 0
            TruvideoSdkVideoRotation.DEGREES_90 -> 90
            TruvideoSdkVideoRotation.DEGREES_180 -> 180
            TruvideoSdkVideoRotation.DEGREES_270 -> 270
        }
    }