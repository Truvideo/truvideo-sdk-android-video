package com.truvideo.sdk.video.examples

import com.truvideo.sdk.video.TruvideoSdkVideo

class CleanNoiseKotlin {
    suspend fun cleanNoise(videoPath: String, resultVideoPath:String){
        try{
            TruvideoSdkVideo.clearNoise(videoPath, resultVideoPath)
            // Handle result
            // the cleaned video will be stored in resultVideoPath
        }catch (exception:Exception){
            // Handle error
            exception.printStackTrace()
        }
    }
}