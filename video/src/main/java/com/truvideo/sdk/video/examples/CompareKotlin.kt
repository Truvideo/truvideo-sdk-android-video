package com.truvideo.sdk.video.examples

import com.truvideo.sdk.video.TruvideoSdkVideo


class CompareKotlin {
    suspend fun compareVideos(videos: List<String>) {
        try {
            val result = TruvideoSdkVideo.compare(videos)
            // Handle result
        } catch (exception: Exception) {
            // Handle error
            exception.printStackTrace()
        }
    }
}