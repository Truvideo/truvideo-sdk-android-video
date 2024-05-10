package com.truvideo.sdk.video.usecases

import android.content.Context
import android.util.Log
import com.truvideo.krisplibrary.Utils
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoMediaManager
import truvideo.sdk.common.exception.TruvideoSdkException
import java.io.File

internal class ClearVideoAudioNoiseUseCase(
    private val context: Context,
    private val mediaManager: TruvideoSdkVideoMediaManager
) {

    companion object {
        private const val TAG = "ClearVideoAudioNoiseUseCase"
        private const val TEMP_AUDIO_FILE_NAME = "audio_temp.wav"
        private const val TEMP_AUDIO_CLEANED_FILE_NAME = "audio_temp_cleaned.wav"
    }

    suspend operator fun invoke(
        videoPath: String,
        resultPath: String
    ) {
        Log.d(TAG, "Video path: $videoPath. Result path: $resultPath")

        val resultFile = File(resultPath)
        tryDeleteFile(resultFile)

        val audioFile = File(context.cacheDir, TEMP_AUDIO_FILE_NAME)
        tryDeleteFile(audioFile)

        val cleanedAudioFile = File(context.cacheDir, TEMP_AUDIO_CLEANED_FILE_NAME)
        tryDeleteFile(cleanedAudioFile)

        try {
            // Extract audio from the video file
            mediaManager.extractAudioFromVideo(
                videoPath = videoPath,
                outputWavPath = audioFile.absolutePath
            )

            Log.d(TAG, "Audio extracted")


            // Get cleaned audio byte array
            val cleanedAudioByteArray = mediaManager.clearNoiseFromAudio(
                context,
                audioPath = audioFile.absolutePath
            )

            Log.d(TAG, "Audio cleared")

            // Move byte array to file
            Utils.byteArrayToFile(cleanedAudioFile, cleanedAudioByteArray)

            // Merge cleaned audio to final video
            mediaManager.mergeAudioWithVideo(
                videoPath = videoPath,
                audioPath = cleanedAudioFile.absolutePath,
                resultPath = resultPath
            )

            Log.d(TAG, "Audio cleared merged into the video")
        } catch (ex: Exception) {
            //TODO: remove this to avoid the final user knows about our errors
            ex.printStackTrace()

            if (ex is TruvideoSdkException) {
                throw ex
            } else {
                throw TruvideoSdkException("Unknown error")
            }
        } finally {
            tryDeleteFile(audioFile)
            tryDeleteFile(cleanedAudioFile)
        }
    }

    private fun tryDeleteFile(file: File) {
        try {
            file.delete()
        } catch (_: Exception) {

        }
    }
}