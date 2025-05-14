package com.truvideo.sdk.video.model

internal enum class TruvideoSdkVideoAudioBitRate {
    Regular;

    val unit: Int
        get() {
            return when (this) {
                Regular -> 320
            }
        }

    val representation: String
        get() {
            return "${unit}k"
        }
}

