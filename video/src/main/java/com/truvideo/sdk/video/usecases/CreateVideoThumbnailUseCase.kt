package com.truvideo.sdk.video.usecases

import android.content.Context
import android.util.Log
import com.truvideo.sdk.video.interfaces.ExecutionResultCode
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoFFmpegAdapter
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile
import com.truvideo.sdk.video.model.TruvideoSdkVideoFileDescriptor
import com.truvideo.sdk.video.model.ffmpegFormat
import truvideo.sdk.common.exception.TruvideoSdkException
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class CreateVideoThumbnailUseCase(
    private val context: Context,
    private val ffmpegAdapter: TruvideoSdkVideoFFmpegAdapter
) {

    suspend operator fun invoke(
        input: TruvideoSdkVideoFile,
        output: TruvideoSdkVideoFileDescriptor,
        position: Duration = 1.seconds,
        width: Int? = null,
        height: Int? = null,
        precise: Boolean = false,
        printLogs: Boolean = false
    ): String {
        val start = System.currentTimeMillis()

        try {
            val inputPath = input.getPath(context)
            val outputPath = output.getPath(context, "png")

            if (!File(inputPath).exists()) {
                throw TruvideoSdkException("Video file not found")
            }

            if (File(outputPath).exists()) {
                File(outputPath).delete()
            }

            if (width != null && width < 0) {
                throw TruvideoSdkException("Invalid width")
            }

            if (height != null && height < 0) {
                throw TruvideoSdkException("Invalid height")
            }

            val command = buildString {
                append("-y ")
                if (!precise) {
                    append("-ss ${position.ffmpegFormat()} -noaccurate_seek ")
                }

                append("-i $inputPath ")

                if (precise) {
                    append("-ss ${position.ffmpegFormat()} ")
                }

                append("-vframes 1 ")
                append("-vf \"scale=${width ?: "-1"}:${height ?: "-1"}\" ")
                append(outputPath)
            }

            if (printLogs) {
                Log.d("TruvideoSdkVideo", "Create thumbnail command: $command")
            }
            val execution = ffmpegAdapter.execute(command)
            val code = execution.code
            if (code != ExecutionResultCode.Success) {
                throw TruvideoSdkException("Error creating thumbnail for this video. FFmpeg code: $code")
            }

            val file = File(outputPath)
            if (!file.exists()) {
                throw TruvideoSdkException("Error creating thumbnail for this video. This might be caused due to an inconsistent timeframe. Timeframe might be greater than video length.")
            }

            return outputPath
        } catch (exception: Exception) {
            exception.printStackTrace()

            if (exception is TruvideoSdkException) {
                throw exception
            } else {
                throw TruvideoSdkException("Unknown error")
            }
        } finally {
            val end = System.currentTimeMillis()
            if (printLogs) {
                Log.d("TruvideoSdkVideo", "Create thumbnail takes ${end - start}ms")
            }
        }
    }
}