package com.truvideo.sdk.video.usecases

import com.truvideo.sdk.video.interfaces.ExecutionResultCode
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoFFmpegAdapter
import truvideo.sdk.common.exception.TruvideoSdkException

internal class RemoveVideoTrackUseCase(
    private val ffmpegAdapter: TruvideoSdkVideoFFmpegAdapter
) {
    suspend operator fun invoke(
        inputPath: String,
        outputPath: String
    ) {
        val command = "-y -i $inputPath -vn -c:a copy $outputPath"
        val session = ffmpegAdapter.execute(command)

        if (session.code != ExecutionResultCode.Success) {
            throw TruvideoSdkException("Error removing audio from video")
        }
    }
}