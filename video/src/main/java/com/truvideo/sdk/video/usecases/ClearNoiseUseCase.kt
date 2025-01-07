package com.truvideo.sdk.video.usecases

import android.content.Context
import com.truvideo.noisecancel.TruvideoNoiseCancellation
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile
import com.truvideo.sdk.video.model.TruvideoSdkVideoFileDescriptor
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import truvideo.sdk.common.exceptions.TruvideoSdkException
import java.io.File

internal class ClearNoiseUseCase(
    private val context: Context,
    private val extractAudioUseCase: ExtractAudioUseCase,
    private val replaceAudioTrackUseCase: ReplaceAudioTrackUseCase
) {

    companion object {
        private const val TEMP_AUDIO_FILE_NAME = "audio_temp"
        private const val TEMP_AUDIO_CLEANED_FILE_NAME = "audio_temp_cleaned"
    }

    private val mutex = Mutex()

    suspend operator fun invoke(
        input: TruvideoSdkVideoFile,
        output: TruvideoSdkVideoFileDescriptor
    ) {
        mutex.withLock {
            val filesToDelete = mutableListOf<String>()

            try {
                // Extract audio from the video file
                val audioPath = extractAudioUseCase(
                    input = input,
                    output = TruvideoSdkVideoFileDescriptor.cache(TEMP_AUDIO_FILE_NAME)
                )
                filesToDelete.add(audioPath)

                // Get cleaned audio byte array
                val cleanedAudioPath = clearNoise(
                    input = TruvideoSdkVideoFile.custom(audioPath),
                    output = TruvideoSdkVideoFileDescriptor.cache(TEMP_AUDIO_CLEANED_FILE_NAME)
                )
                filesToDelete.add(cleanedAudioPath)

                // Merge cleaned audio to final video
                replaceAudioTrackUseCase(
                    videoInput = input,
                    audioInput = TruvideoSdkVideoFile.custom(cleanedAudioPath),
                    output = output
                )
            } catch (exception: Exception) {
                exception.printStackTrace()
                if (exception is TruvideoSdkException) {
                    throw exception
                } else {
                    throw TruvideoSdkException("Unknown error")
                }
            } finally {
                filesToDelete.forEach {
                    tryDeleteFile(it)
                }
            }
        }
    }

    private val noiseCancellation = TruvideoNoiseCancellation()

    private suspend fun clearNoise(
        input: TruvideoSdkVideoFile,
        output: TruvideoSdkVideoFileDescriptor,
    ): String {
        val inputPath = input.getPath(context)
        val inputExtension = File(inputPath).extension
        return noiseCancellation.call(
            context = context,
            inputPath = input.getPath(context),
            outputPath = output.getPath(context, inputExtension),
        )
    }

    private fun tryDeleteFile(path: String) {
        try {
            File(path).delete()
        } catch (_: Exception) {
        }
    }
}