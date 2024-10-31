package com.truvideo.sdk.video.model

internal enum class TruvideoSdkVideoAudioFormat(val description: String) {
    Wav("wav");

    val fileExtension: String
        get() {
            return when (this) {
                Wav -> "wav"
            }
        }
}