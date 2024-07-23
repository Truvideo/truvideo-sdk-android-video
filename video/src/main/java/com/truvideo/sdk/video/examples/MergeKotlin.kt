package com.truvideo.sdk.video.examples

import com.truvideo.sdk.video.TruvideoSdkVideo

class MergeKotlin {

    suspend fun mergeVideos(videos:List<String>, resultVideoPath:String){
        try{
            val builder = TruvideoSdkVideo.MergeBuilder(videos, resultVideoPath)
            val request = builder.build()
            request.process()

            // Handle result
            // the merged video its on 'resultVideoPath'
        }catch (exception:Exception){
            //Handle error
            exception.printStackTrace()
        }
    }
}