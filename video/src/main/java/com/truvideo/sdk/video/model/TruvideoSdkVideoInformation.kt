package com.truvideo.sdk.video.model

data class TruvideoSdkVideoInformation(
    val path: String,
    val durationMillis: Int,
    val width: Int,
    val height: Int,
    val size: Long,
    val withVideo: Boolean,
    val videoCodec: String,
    val videoPixelFormat: String,
    val withAudio: Boolean,
    val audioCodec: String,
    val audioSampleRate: Int,
    val rotation: Int
)
