package com.truvideo.sdk.video.usecases

import android.content.Context
import android.util.Log
import com.truvideo.sdk.video.interfaces.ExecutionResultCode
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoFFmpegAdapter
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile
import com.truvideo.sdk.video.model.TruvideoSdkVideoFileDescriptor
import com.truvideo.sdk.video.model.TruvideoSdkVideoRotation
import com.truvideo.sdk.video.model.ffmpegDegrees
import truvideo.sdk.common.exceptions.TruvideoSdkException
import java.io.File
import java.util.Formatter

internal class EditVideoUseCase(
    private val context: Context,
    private val getVideoInfoUseCase: GetVideoInfoUseCase,
    private val ffmpegAdapter: TruvideoSdkVideoFFmpegAdapter
) {

    companion object {
        const val TAG = "TruvideoSdkVideo [EditVideoUseCase]"
    }

    suspend operator fun invoke(
        input: TruvideoSdkVideoFile,
        output: TruvideoSdkVideoFileDescriptor,
        startPosition: Long? = null,
        endPosition: Long? = null,
        rotation: TruvideoSdkVideoRotation? = null,
        volume: Float = 1f,
        printLogs: Boolean = false
    ): String {
        val start = System.currentTimeMillis()
        val shouldTrim = startPosition != null || endPosition != null

        try {
            val inputPath = input.getPath(context)
            val extension = File(inputPath).extension
            val outputPath = output.getPath(context, extension)

            val info = getVideoInfoUseCase(input)

            val command = buildString {
                append("-y ")

                append("-i $inputPath ")

                if (shouldTrim) {
                    val effectiveStart = startPosition ?: 0L
                    append("-ss ${timeToFFmpeg(effectiveStart)} ")
                }

                if (shouldTrim) {
                    val effectiveEnd = endPosition ?: info.durationMillis
                    append("-to ${timeToFFmpeg((effectiveEnd))} ")
                }

                if (rotation != null) {
                    append("-metadata:s:v:0 rotate=${rotation.ffmpegDegrees} ")
                }

                append("-c:v copy ")

                when (volume) {
                    0f -> append("-an ")
                    1f -> append("-c:a copy ")
                    else -> {
                        val audioCodec = when (extension) {
                            "webm" -> "libopus"
                            else -> "aac"
                        }
                        append("-af \"volume=$volume\" -c:a $audioCodec ")
                    }
                }

                append("-reset_timestamps 1 ")
                append(outputPath)
            }

            if (printLogs) {
                Log.d(TAG, "Command: $command")
            }

            val result = ffmpegAdapter.execute(command)
            if (result.code != ExecutionResultCode.Success) {
                val message = result.output
                Log.d("TruvideoSdkVideo", "Error editing the video. FFmpeg code: ${result.code}. Message: $message")
                throw TruvideoSdkException("Error editing the video")
            }

            val file = File(outputPath)
            if (!file.exists()) {
                throw TruvideoSdkException("Error editing the video. This might be caused due to an inconsistent timeframe. Timeframe might be greater than video length.")
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
                Log.d("TruvideoSdkVideo", "EditVideoUseCase takes ${end - start}ms")
            }
        }
    }

    private fun timeToFFmpeg(timeMs: Long): String {
        val totalMilliseconds = timeMs.toInt()
        val totalSeconds = totalMilliseconds / 1000
        val milliseconds = totalMilliseconds % 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        val mFormatter = Formatter()
        return mFormatter.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds).toString()
    }

}