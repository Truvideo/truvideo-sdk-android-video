package com.truvideo.sdk.video.examples

import com.truvideo.sdk.video.TruvideoSdkVideo
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile
import com.truvideo.sdk.video.model.TruvideoSdkVideoFileDescriptor
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequest

class EncodeKotlin {

    suspend fun encodeVideo(
        input: TruvideoSdkVideoFile,
        output: TruvideoSdkVideoFileDescriptor
    ) {
        try {
            val builder = TruvideoSdkVideo.EncodeBuilder(input, output)

            // Set custom video resolution
            // builder.width = 1000
            // builder.height = 1000

            val request: TruvideoSdkVideoRequest = builder.build()
            val resultPath: String = request.process()

            // Handle result
            // the merged video its on 'resultPath'
        } catch (exception: Exception) {
            //Handle error
            exception.printStackTrace()
        }
    }
}