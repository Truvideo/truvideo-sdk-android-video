package com.truvideo.sdk.video.usecases

import android.util.Log
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoFFmpegAdapter
import com.truvideo.sdk.video.model.TruvideoSdkVideoException
import com.truvideo.sdk.video.model.TruvideoSdkVideoInformation
import truvideo.sdk.common.exception.TruvideoSdkException

internal class GetVideoInfoUseCase(
    private val ffmpegAdapter: TruvideoSdkVideoFFmpegAdapter
) {
    suspend operator fun invoke(videoPath: String): TruvideoSdkVideoInformation {
        try {
            val information = ffmpegAdapter.getInformation(videoPath)
            Log.d(TAG, "Information: $information")
            val videoSize = Pair(information.width, information.height)
            val videoWidth = videoSize.first
            val videoHeight = videoSize.second

            if (videoWidth == 0) {
                throw TruvideoSdkVideoException("Invalid video width")
            }

            if (videoHeight == 0) {
                throw TruvideoSdkVideoException("Invalid video height")
            }

            return TruvideoSdkVideoInformation(
                path = videoPath,
                durationMillis = information.durationMillis,
                width = videoWidth,
                height = videoHeight,
                size = information.size,
                withVideo = information.withVideo,
                videoCodec = information.videoCodec,
                videoPixelFormat = information.videoPixelFormat,
                withAudio = information.withAudio,
                audioCodec = information.audioCodec,
                audioSampleRate = information.audioSampleRate,
                rotation = information.rotation
            )
        } catch (ex: Exception) {
            ex.printStackTrace()

            if (ex is TruvideoSdkException) {
                throw ex
            }

            throw TruvideoSdkException(ex.message ?: "")
        }
    }

    companion object {
        private const val TAG = "TruvideoSdkVideo"
    }
}