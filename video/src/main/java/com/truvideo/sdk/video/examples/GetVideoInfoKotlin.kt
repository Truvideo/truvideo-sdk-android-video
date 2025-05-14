package com.truvideo.sdk.video.examples

import com.truvideo.sdk.video.TruvideoSdkVideo
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile
import com.truvideo.sdk.video.model.TruvideoSdkVideoInformation

class GetVideoInfoKotlin {

    suspend fun getVideoInfo(input: TruvideoSdkVideoFile) {
        try {
            val info: TruvideoSdkVideoInformation = TruvideoSdkVideo.getInfo(input)
            // Handle video information
        } catch (exception: Exception) {
            exception.printStackTrace()
            // Handle error
        }
    }
}