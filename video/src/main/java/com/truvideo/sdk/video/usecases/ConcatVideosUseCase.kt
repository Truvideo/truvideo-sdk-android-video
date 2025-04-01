package com.truvideo.sdk.video.usecases

import android.content.Context
import android.util.Log
import com.truvideo.sdk.video.interfaces.ExecutionResultCode
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoFFmpegAdapter
import com.truvideo.sdk.video.interfaces.isSuccess
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile
import com.truvideo.sdk.video.model.TruvideoSdkVideoFileDescriptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import truvideo.sdk.common.exceptions.TruvideoSdkException
import java.io.File
import kotlin.coroutines.suspendCoroutine

internal class ConcatVideosUseCase(
    private val context: Context,
    private val ffmpegAdapter: TruvideoSdkVideoFFmpegAdapter
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private suspend fun createTempFile(
        paths: List<String>
    ): File {
        return suspendCoroutine { cont ->
            scope.launch {
                try {
                    val path = "${context.cacheDir.path}/concat_temp.txt"
                    val file = File(path)
                    if (file.exists()) file.delete()
                    file.writeText(paths.joinToString("\n") { "file $it" })
                    cont.resumeWith(Result.success(file))
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    Log.d("TruvideoSdkVideo", "Error creating temp file ${exception.localizedMessage}")
                    cont.resumeWith(Result.failure(exception))
                }
            }
        }
    }

    suspend operator fun invoke(
        input: List<TruvideoSdkVideoFile>,
        output: TruvideoSdkVideoFileDescriptor
    ): String {
        val start = System.currentTimeMillis()
        val filesToDelete = mutableListOf<String>()

        try {
            if (input.isEmpty()) throw TruvideoSdkException("Invalid input")

            val outputPath = output.getPath(context, File(input.first().getPath(context)).extension)

            val tempFile = createTempFile(input.map { it.getPath(context) })
            filesToDelete.add(tempFile.path)

            val command = "-y -f concat -safe 0 -i ${tempFile.path} -c copy $outputPath"

            val sessionResult = ffmpegAdapter.execute(command)
            if (!sessionResult.code.isSuccess) {
                Log.d("TruvideoSdkVideo", "Failed concatenating videos. ${sessionResult.output}")
                throw TruvideoSdkException("Unknown error")
            }

            return outputPath
        } catch (exception: Exception) {
            Log.d("TruvideoSdkVideo", "Failed to concatenate videos. ${exception.localizedMessage}")
            if (exception is TruvideoSdkException) {
                throw exception
            } else {
                throw TruvideoSdkException("Unknown error")
            }
        } finally {
            // Delete all files
            filesToDelete.forEach {
                try {
                    File(it).delete()
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }

            val end = System.currentTimeMillis()
            Log.d("TruvideoSdkVideo", "Concat request completed. Time: ${end - start}")
        }
    }

    fun concat(
        input: List<TruvideoSdkVideoFile>,
        output: TruvideoSdkVideoFileDescriptor,
        onRequestCreated: (id: Long) -> Unit = {},
        progressCallback: (progress: Long) -> Unit = {},
        callback: (result: String) -> Unit = {},
        callbackCanceled: () -> Unit = {},
        callbackError: (exception: TruvideoSdkException) -> Unit = {}
    ) {
        scope.launch {
            val start = System.currentTimeMillis()
            val filesToDelete = mutableListOf<String>()

            try {
                if (input.isEmpty()) throw TruvideoSdkException("Invalid input")

                val outputPath = output.getPath(context, File(input.first().getPath(context)).extension)

                val tempFile = createTempFile(input.map { it.getPath(context) })
                filesToDelete.add(tempFile.path)

                val command = "-y -f concat -safe 0 -i ${tempFile.path} -c copy $outputPath"

                ffmpegAdapter.executeAsync(
                    command = command,
                    onRequestCreated = { onRequestCreated(it) },
                    progressCallback = { progressCallback(it) },
                    callback = {

                        // Delete all files
                        filesToDelete.forEach { path ->
                            try {
                                File(path).delete()
                                Log.d("TruvideoSdkVideo", "File $path deleted")
                            } catch (exception: Exception) {
                                exception.printStackTrace()
                            }
                        }

                        val end = System.currentTimeMillis()

                        when (it.code) {
                            ExecutionResultCode.Success -> {
                                Log.d("TruvideoSdkVideo", "Concat request completed. Time: ${end - start}")
                                callback(outputPath)
                            }

                            ExecutionResultCode.Canceled -> {
                                Log.d("TruvideoSdkVideo", "Concat request canceled. Time: ${end - start}")
                                callbackCanceled()
                            }

                            ExecutionResultCode.Error -> {
                                Log.d("TruvideoSdkVideo", "Error concatenating videos. Time: ${end - start}. ${it.output}")
                                callbackError(TruvideoSdkException("Unknown error"))
                            }

                        }
                    }
                )
            } catch (exception: Exception) {
                Log.d("TruvideoSdkVideo", "Failed to concatenate videos. ${exception.localizedMessage}")
                if (exception is TruvideoSdkException) {
                    callbackError(exception)
                } else {
                    callbackError(TruvideoSdkException("Unknown error"))
                }
            }
        }
    }
}