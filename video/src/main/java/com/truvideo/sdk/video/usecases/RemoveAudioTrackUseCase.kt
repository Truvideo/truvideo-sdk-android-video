package com.truvideo.sdk.video.usecases

import com.truvideo.sdk.video.interfaces.ExecutionResultCode
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoFFmpegAdapter
import truvideo.sdk.common.exceptions.TruvideoSdkException

internal class RemoveAudioTrackUseCase(
    private val ffmpegAdapter: TruvideoSdkVideoFFmpegAdapter
) {
    suspend operator fun invoke(
        inputPath: String,
        resultPath: String
    ) {
        val command = "-y -i $inputPath -an -c:v copy $resultPath"
        val session = ffmpegAdapter.execute(command)

        if (session.code != ExecutionResultCode.Success) {
            throw TruvideoSdkException("Error removing audio from video")
        }
    }
}