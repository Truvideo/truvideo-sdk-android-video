package com.truvideo.sdk.video.usecases

import android.content.Context
import com.truvideo.krisplibrary.Krisp
import com.truvideo.krisplibrary.Utils
import com.truvideo.krisplibrary.listener.KrispListener
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile
import com.truvideo.sdk.video.model.TruvideoSdkVideoFileDescriptor
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import truvideo.sdk.common.exception.TruvideoSdkException
import java.io.File
import kotlin.coroutines.suspendCoroutine

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
                val cleanedAudioByteArray = clearNoise(TruvideoSdkVideoFile.cache(TEMP_AUDIO_FILE_NAME, File(audioPath).extension))

                // Create cleaned audio file
                val cleanedAudioFile = TruvideoSdkVideoFile.cache(
                    TEMP_AUDIO_CLEANED_FILE_NAME,
                    File(audioPath).extension
                )
                val cleanedAudioFilePath = cleanedAudioFile.getPath(context)
                tryDeleteFile(cleanedAudioFilePath)
                filesToDelete.add(cleanedAudioFilePath)

                // Move byte array to file
                Utils.byteArrayToFile(File(cleanedAudioFilePath), cleanedAudioByteArray)

                // Merge cleaned audio to final video
                replaceAudioTrackUseCase(
                    videoInput = input,
                    audioInput = cleanedAudioFile,
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


    private suspend fun clearNoise(input: TruvideoSdkVideoFile): ByteArray {
        return suspendCoroutine { continuation ->
            try {
                val bytes = Utils.fileToByteArray(File(input.getPath(context)))
                Krisp().call(context,
                    bytes,
                    object : KrispListener {
                        override fun onSuccess(byteArray: ByteArray?) {
                            if (byteArray == null) {
                                continuation.resumeWith(Result.failure(TruvideoSdkException("File not found")))
                            } else {
                                continuation.resumeWith(Result.success(byteArray))
                            }
                        }

                        override fun onError() {
                            continuation.resumeWith(Result.failure(TruvideoSdkException("Unknown error")))
                        }
                    }
                )
            } catch (ex: Exception) {
                ex.printStackTrace()
                if (ex is TruvideoSdkException) {
                    continuation.resumeWith(Result.failure(ex))
                } else {
                    continuation.resumeWith(Result.failure(TruvideoSdkException("Unknown error")))
                }
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