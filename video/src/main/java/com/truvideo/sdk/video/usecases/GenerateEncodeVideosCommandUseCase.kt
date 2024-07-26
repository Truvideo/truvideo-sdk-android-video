package com.truvideo.sdk.video.usecases

import android.util.Log
import com.truvideo.sdk.video.model.TruvideoSdkVideoException
import com.truvideo.sdk.video.model.TruvideoSdkVideoFrameRate
import com.truvideo.sdk.video.model.TruvideoSdkVideoInformation
import com.truvideo.sdk.video.model.TruvideoSdkVideoVideoCodec

internal class GenerateEncodeVideosCommandUseCase(
    private val getVideoInfoUseCase: GetVideoInfoUseCase
) {

    companion object {
        private const val TAG = "EncodeVideoUtils"
    }

    suspend operator fun invoke(
        videoPath: String,
        resultPath: String,
        receivedWidth: Int?,
        receivedHeight: Int?,
        videoCodec: TruvideoSdkVideoVideoCodec,
        framesRate: TruvideoSdkVideoFrameRate
    ): String {
        if (receivedWidth != null && receivedWidth < 128) {
            throw TruvideoSdkVideoException("Width must be 0 or greater than or equal to 128")
        }

        if (receivedHeight != null && receivedHeight < 128) {
            throw TruvideoSdkVideoException("Height must be 0 or greater than or equal to 128")
        }

        var maxWidth: Int
        var maxHeight: Int

        val videoInfo: TruvideoSdkVideoInformation = getVideoInfoUseCase(videoPath)

        maxWidth = receivedWidth ?: videoInfo.width
        maxHeight = receivedHeight ?: videoInfo.height

        maxWidth = convertToEven(maxWidth)
        maxHeight = convertToEven(maxHeight)


        // Scale
        val scale = if (maxWidth == videoInfo.width && maxHeight == videoInfo.height) {
            ""
        } else {
            val scale = " -vf scale=$maxWidth:$maxHeight:force_original_aspect_ratio=decrease,"

            // Pad
            val pad = "pad=$maxWidth:$maxHeight:(ow-iw)/2:(oh-ih)/2,setsar=1"

            scale + pad
        }

        val command = "-y -i $videoPath -r ${framesRate.value}$scale -c:v ${videoCodec.value}${
            getTagFor(
                videoCodec
            )
        } -c:a aac -preset superfast $resultPath"
        Log.d(TAG, "Command: $command")

        return command
    }

    private fun convertToEven(number: Int): Int {
        if (number % 2 != 0) {
            return number + 1
        }
        return number
    }

    private fun getTagFor(videoCodec: TruvideoSdkVideoVideoCodec): String {
        return if (videoCodec == TruvideoSdkVideoVideoCodec.h265) {
            " -tag:v hvc1"
        } else {
            ""
        }
    }


}