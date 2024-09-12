package com.truvideo.sdk.video.usecases

import com.arthenica.mobileffmpeg.Config
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoFFmpegAdapter
import com.truvideo.sdk.video.model.TruvideoSdkVideoException
import com.truvideo.sdk.video.model.ffmpegFormat
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class CreateVideoThumbnailUseCase(
    private val ffmpegAdapter: TruvideoSdkVideoFFmpegAdapter
) {

    suspend operator fun invoke(
        videoPath: String,
        resultPath: String,
        position: Duration = 1.seconds,
        width: Int? = null,
        height: Int? = null,
    ): String {
        try {
            if (!File(videoPath).exists()) {
                throw TruvideoSdkVideoException("Video file not found")
            }

            if (File(resultPath).exists()) {
                File(resultPath).delete()
            }

            if (width != null && width < 0) {
                throw TruvideoSdkVideoException("Invalid width")
            }

            if (height != null && height < 0) {
                throw TruvideoSdkVideoException("Invalid height")
            }

            val command = arrayOf(
                "-y",
                "-i",
                videoPath,
                "-ss",
                position.ffmpegFormat(),
                "-vframes",
                "1",
                "-vf",
                "scale=${width ?: "-1"}:${height ?: "-1"}",
                resultPath
            )


            val execution = ffmpegAdapter.executeArray(command)
            val code = execution.code
            if (code != Config.RETURN_CODE_SUCCESS) {
                throw TruvideoSdkVideoException("Error creating thumbnail for this video. FFmpeg code: $code")
            }

            val file = File(resultPath)
            if (!file.exists()) {
                throw TruvideoSdkVideoException("Error creating thumbnail for this video. This might be caused due to an inconsistent timeframe. Timeframe might be greater than video length.")
            }

            return resultPath
        } catch (exception: Exception) {
            exception.printStackTrace()

            if (exception is TruvideoSdkVideoException) {
                throw exception
            } else {
                throw TruvideoSdkVideoException(exception.localizedMessage ?: "Unknown error")
            }
        }
    }
}