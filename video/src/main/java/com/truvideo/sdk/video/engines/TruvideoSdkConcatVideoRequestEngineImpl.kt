package com.truvideo.sdk.video.engines

import android.content.Context
import android.util.Log
import com.arthenica.mobileffmpeg.Config
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoAuthAdapter
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoCancelCallback
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoFFmpegAdapter
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoJoinCallback
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoRequestEngine
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoRequestRepository
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestStatus
import com.truvideo.sdk.video.usecases.CompareVideosUseCase
import com.truvideo.sdk.video.usecases.GenerateConcatVideosCommandUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import truvideo.sdk.common.exception.TruvideoSdkException
import java.io.File
import kotlin.coroutines.suspendCoroutine

internal class TruvideoSdkConcatVideoRequestEngineImpl(
    private val context: Context,
    private val videoRequestRepository: TruvideoSdkVideoRequestRepository,
    private val generateConcatVideosCommandUseCase: GenerateConcatVideosCommandUseCase,
    private val authAdapter: TruvideoSdkVideoAuthAdapter,
    private val compareVideosUseCase: CompareVideosUseCase,
    private val ffmpegAdapter: TruvideoSdkVideoFFmpegAdapter
) : TruvideoSdkVideoRequestEngine {

    override suspend fun process(id: String) = concat(id)

    override fun process(
        id: String,
        callback: TruvideoSdkVideoJoinCallback
    ) = concat(id, callback)

    override suspend fun cancel(id: String) {
        try {
            authAdapter.validateAuthentication()
            val request = videoRequestRepository.getById(id) ?: throw TruvideoSdkException("Video request not found")

            // Valid state
            if (request.status != TruvideoSdkVideoRequestStatus.PROCESSING) {
                throw TruvideoSdkException("The request can't be cancelled")
            }

            // Cancel ffmpeg command
            val commandId = request.commandId
            if (commandId != null) {
                ffmpegAdapter.cancel(commandId)
            }

            // Update
            videoRequestRepository.tryChangeStatusToCanceled(id)
        } catch (exception: Exception) {
            exception.printStackTrace()

            val message = if (exception is TruvideoSdkException) exception.message else "Unknown error"
            videoRequestRepository.tryChangeStatusToError(id, message)

            if (exception is TruvideoSdkException) {
                throw exception
            } else {
                throw TruvideoSdkException("Unknown error")
            }
        }
    }

    override fun cancel(
        id: String,
        callback: TruvideoSdkVideoCancelCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                cancel(id)
                callback.onCanceled()
            } catch (exception: Exception) {
                if (exception is TruvideoSdkException) {
                    callback.onError(exception)
                } else {
                    callback.onError(TruvideoSdkException(exception.message ?: ""))
                }
            }
        }
    }

    override suspend fun delete(id: String) {
        authAdapter.validateAuthentication()
        videoRequestRepository.delete(id)
    }

    private suspend fun concat(id: String) {
        authAdapter.validateAuthentication()

        suspendCoroutine { cont ->
            CoroutineScope(Dispatchers.IO).launch {
                val cacheDir = context.cacheDir
                val tempFile = File.createTempFile("temp", ".txt", cacheDir)

                try {

                    // Fetch the request
                    val request = videoRequestRepository.getById(id) ?: throw TruvideoSdkException("Video request not found")

                    // Validate state
                    val validStates = listOf(
                        TruvideoSdkVideoRequestStatus.IDLE,
                        TruvideoSdkVideoRequestStatus.ERROR,
                        TruvideoSdkVideoRequestStatus.CANCELED
                    )
                    if (!validStates.contains(request.status)) {
                        throw TruvideoSdkException("Video request in an invalid state")
                    }

                    // Validate data
                    val data = request.concatData ?: throw TruvideoSdkException("Invalid request data")

                    // Update status
                    videoRequestRepository.tryChangeStatusToProcessing(id)

                    // Compare videos
                    val compareResult = compareVideosUseCase(data.videoPaths)
                    if (!compareResult) {
                        throw TruvideoSdkException("The videos are not valid for concatenation")
                    }

                    // Generate command
                    val command = generateConcatVideosCommandUseCase(
                        videoPaths = data.videoPaths,
                        resultPath = data.resultPath,
                        tempFilePath = tempFile.path
                    )

                    // Execute
                    val commandId = ffmpegAdapter.executeAsync(
                        command,
                        callback = { _, code, _ ->
                            CoroutineScope(Dispatchers.IO).launch {
                                when (code) {
                                    Config.RETURN_CODE_SUCCESS -> {
                                        Log.d("TruvideoSdkVideo", "Request completed")
                                        videoRequestRepository.tryChangeStatusToCompleted(id)
                                        cont.resumeWith(Result.success(Unit))
                                    }

                                    Config.RETURN_CODE_CANCEL -> {
                                        Log.d("TruvideoSdkVideo", "Request canceled")
                                        cont.resumeWith(Result.success(Unit))
                                    }

                                    else -> {
                                        Log.d("TruvideoSdkVideo", "Request error")
                                        val ex = TruvideoSdkException("Error encoding the video in merge process")
                                        videoRequestRepository.tryChangeStatusToError(id, ex.message)
                                        cont.resumeWith(Result.failure(ex))
                                    }
                                }
                            }

                        }
                    )

                    videoRequestRepository.tryUpdateCommandId(id, commandId)
                } catch (ex: Exception) {
                    ex.printStackTrace()

                    val message = if (ex is TruvideoSdkException) ex.message else "Unknown error"
                    videoRequestRepository.tryChangeStatusToError(id, message)

                    val e = if (ex is TruvideoSdkException) ex else TruvideoSdkException(ex.message ?: "")
                    cont.resumeWith(Result.failure(e))
                } finally {
                    tempFile.delete()
                }
            }
        }
    }

    private fun concat(
        id: String,
        callback: TruvideoSdkVideoJoinCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                concat(id)
                callback.onReady()
            } catch (exception: Exception) {
                if (exception is TruvideoSdkException) {
                    callback.onError(exception)
                } else {
                    callback.onError(TruvideoSdkException(exception.message ?: ""))
                }
            }
        }
    }

}