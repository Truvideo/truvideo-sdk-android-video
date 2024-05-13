package com.truvideo.sdk.video.managers

import android.content.Context
import android.util.Log
import com.arthenica.mobileffmpeg.Config
import com.truvideo.krisplibrary.Krisp
import com.truvideo.krisplibrary.Utils
import com.truvideo.krisplibrary.listener.KrispListener
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoFFmpegAdapter
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoMediaManager
import com.truvideo.sdk.video.model.AudioFormat
import com.truvideo.sdk.video.model.BitRate
import truvideo.sdk.common.exception.TruvideoSdkException
import java.io.File
import kotlin.coroutines.suspendCoroutine

internal class TruvideoSdkVideoMediaManagerImpl(
    private val ffmpegAdapter: TruvideoSdkVideoFFmpegAdapter
) : TruvideoSdkVideoMediaManager {
    companion object {
        private const val TAG = "MediaManagerImpl"
    }

    override suspend fun extractAudioFromVideo(
        videoPath: String,
        outputWavPath: String,
        format: AudioFormat,
        audioChannels: Int,
        samplingRate: Int,
        bitRate: BitRate
    ): File {
        try {
            val cmd = arrayOf(
                "-i", videoPath, "-vn",         // Disable video recording
                "-ac", audioChannels.toString(),    // Set audio channels to 1 (monaural)
                "-ar", samplingRate.toString(),     // Set audio sampling rate to 16000 Hz
                "-ab", bitRate.toString(),          // Set audio bit rate to 320 Kbps
                "-f", format.description,           // Output format is WAV
                outputWavPath
            )

            val execution = ffmpegAdapter.executeArray(cmd)
            val code = execution.code
            if (code != Config.RETURN_CODE_SUCCESS) {
                throw Exception("Error extracting audio. FFmpeg Code: $code")
            }

            return File(outputWavPath)
        } catch (ex: Exception) {
            //TODO: remove this to avoid the final user knows about our errors
            ex.printStackTrace()

            if (ex is TruvideoSdkException) {
                throw ex
            } else {
                throw TruvideoSdkException(ex.message ?: "")
            }
        }
    }

    override suspend fun mergeAudioWithVideo(
        videoPath: String, audioPath: String, resultPath: String
    ): File {
        try {
            Log.d(TAG, "Video path $videoPath. AudioPath: $audioPath. Result Path: $resultPath")

            val command = arrayOf(
                "-i",
                videoPath,
                "-i",
                audioPath,
                "-c:v",
                "copy",
                "-c:a",
                "aac",
                "-map",
                "0:v:0",
                "-map",
                "1:a:0",
                resultPath
            )

            val execution = ffmpegAdapter.executeArray(command)
            val code = execution.code
            if (code != Config.RETURN_CODE_SUCCESS) {
                throw Exception("Error merging cleaned audio. FFmpeg code: $code")
            }

            return File(resultPath)
        } catch (ex: Exception) {
            //TODO: remove this to avoid the final user knows about our errors
            ex.printStackTrace()

            if (ex is TruvideoSdkException) {
                throw ex
            } else {
                throw TruvideoSdkException(ex.message ?: "")
            }
        }
    }

    override suspend fun clearNoiseFromAudio(
        context: Context, audioPath: String
    ): ByteArray {
        return suspendCoroutine { continuation ->
            try {
                Krisp().call(context,
                    Utils.fileToByteArray(File(audioPath)),
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
                    })
            } catch (ex: Exception) {
                ex.printStackTrace()
                if (ex is TruvideoSdkException) {
                    continuation.resumeWith(Result.failure(ex))
                } else {
                    continuation.resumeWith(Result.failure(TruvideoSdkException(ex.message ?: "")))
                }

            }
        }
    }

    override suspend fun rotateVideo(
        context: Context,
        videoPath: String,
        videoOutputPath: String
    ): File {
        try {
//            Log.d(TAG, "Video path $videoPath. AudioPath: $audioPath. Result Path: $resultPath")

//            val command = arrayOf(
//                "-i",
//                videoPath,
//                "-metadata:s:v",
//                "rotate=0",
//                "-vf",
//                "transpose=1",
//                "-c:v",
//                "libx264",
//                "-crf",
//                "23",
//                "-acodec",
//                "copy",
//                videoOutputPath,
//            )

            val command = arrayOf(
                "-i",
                videoPath,
                "-metadata:s:v",
                "rotate=270",
                "copy",
                videoOutputPath,
            )

            val result = ffmpegAdapter.executeArray(command)
            if (result.code != Config.RETURN_CODE_SUCCESS) {
                throw Exception("Error rotating video, FFmpeg code: ${result.code}")
            }

            return File(videoOutputPath)
        } catch (ex: Exception) {
            //TODO: remove this to avoid the final user knows about our errors
            ex.printStackTrace()

            if (ex is TruvideoSdkException) {
                throw ex
            } else {
                throw TruvideoSdkException(ex.message ?: "")
            }
        }
    }

    override suspend fun removeAudioTrack(
        context: Context,
        videoPath: String,
        videoOutputPath: String
    ): File {
        try {
//            Log.d(TAG, "Video path $videoPath. AudioPath: $audioPath. Result Path: $resultPath")

            val command = arrayOf(
                "-i",
                videoPath,
                "-an",
                "-c:v",
                "copy",
                videoOutputPath,
            )

            val result = ffmpegAdapter.executeArray(command)
            if (result.code != Config.RETURN_CODE_SUCCESS) {
                throw Exception("Error removing audio from video, FFmpeg code: ${result.code}")
            }

            return File(videoOutputPath)
        } catch (ex: Exception) {
            //TODO: remove this to avoid the final user knows about our errors
            ex.printStackTrace()

            if (ex is TruvideoSdkException) {
                throw ex
            } else {
                throw TruvideoSdkException(ex.message ?: "")
            }
        }
    }
}