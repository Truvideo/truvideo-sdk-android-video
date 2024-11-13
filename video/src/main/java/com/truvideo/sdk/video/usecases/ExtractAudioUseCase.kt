package com.truvideo.sdk.video.usecases

import android.content.Context
import com.truvideo.sdk.video.interfaces.ExecutionResultCode
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoFFmpegAdapter
import com.truvideo.sdk.video.model.TruvideoSdkVideoAudioBitRate
import com.truvideo.sdk.video.model.TruvideoSdkVideoAudioFormat
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile
import com.truvideo.sdk.video.model.TruvideoSdkVideoFileDescriptor
import truvideo.sdk.common.exceptions.TruvideoSdkException
import java.io.File

internal class ExtractAudioUseCase(
    private val context: Context,
    private val ffmpegAdapter: TruvideoSdkVideoFFmpegAdapter
) {
    suspend operator fun invoke(
        input: TruvideoSdkVideoFile,
        output: TruvideoSdkVideoFileDescriptor,
        format: TruvideoSdkVideoAudioFormat = TruvideoSdkVideoAudioFormat.Wav,
        audioChannels: Int = 1,
        samplingRate: Int = 16_000,
        bitRate: TruvideoSdkVideoAudioBitRate = TruvideoSdkVideoAudioBitRate.Regular
    ): String {
        val outputPath = output.getPath(context, format.fileExtension)
        tryDeleteFile(outputPath)

        try {
            val cmd = arrayOf(
                "-i", input.getPath(context),
                "-vn",                              // Disable video recording
                "-ac", audioChannels.toString(),    // Set audio channels
                "-ar", samplingRate.toString(),     // Set audio sampling rate
                "-ab", bitRate.representation,      // Set audio bit rate
                "-f", format.description,           // Output format
                outputPath
            )

            val execution = ffmpegAdapter.executeArray(cmd)
            val code = execution.code
            if (code != ExecutionResultCode.Success) {
                throw TruvideoSdkException("Error extracting audio. FFmpeg Code: $code")
            }

            return outputPath
        } catch (ex: Exception) {
            tryDeleteFile(outputPath)
            ex.printStackTrace()

            if (ex is TruvideoSdkException) {
                throw ex
            } else {
                throw TruvideoSdkException("Something went wrong trying to extract audio")
            }
        }
    }

    private fun tryDeleteFile(path: String) {
        try {
            File(path).delete()
        } catch (_: Exception) {
        }
    }

}