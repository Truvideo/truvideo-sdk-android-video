package com.truvideo.sdk.video.examples

import com.truvideo.sdk.video.TruvideoSdkVideo

class GetVideoInfoKotlin {

    suspend fun getVideoInfo(videoPath: String) {
        try {
            val info = TruvideoSdkVideo.getInfo(videoPath)

            // Handle video information
            val duration: Int = info.durationMillis
            val width: Int = info.width
            val height: Int = info.height
            val videoCodec: String = info.videoCodec
            val audioCodec: String = info.audioCodec
            val rotation: Int = info.rotation
        } catch (exception: Exception) {
            exception.printStackTrace()
            // Handle error
        }
    }
}