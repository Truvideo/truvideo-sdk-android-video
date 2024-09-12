package com.truvideo.sdk.video.engines

import android.util.Log
import com.arthenica.mobileffmpeg.Config
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoAuthAdapter
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoCallback
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoFFmpegAdapter
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoRequestEngine
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoRequestRepository
import com.truvideo.sdk.video.model.TruvideoSdkVideoException
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestStatus
import com.truvideo.sdk.video.usecases.GenerateEncodeVideosCommandUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.suspendCoroutine

internal class TruvideoSdkEncodeVideoRequestEngineImpl(
    private val videoRequestRepository: TruvideoSdkVideoRequestRepository,
    private val authAdapter: TruvideoSdkVideoAuthAdapter,
    private val ffmpegAdapter: TruvideoSdkVideoFFmpegAdapter,
    private val generateEncodeVideosCommandUseCase: GenerateEncodeVideosCommandUseCase
) : TruvideoSdkVideoRequestEngine {

    private val scope = CoroutineScope(Dispatchers.IO)

    override suspend fun process(id: String) {
        authAdapter.validateAuthentication()

        suspendCoroutine { cont ->
            scope.launch {
                try {
                    // Fetch the request
                    val request = videoRequestRepository.getById(id) ?: run {
                        throw TruvideoSdkVideoException("Video request not found")
                    }

                    // Validate state
                    val validStates = listOf(
                        TruvideoSdkVideoRequestStatus.IDLE,
                        TruvideoSdkVideoRequestStatus.ERROR,
                        TruvideoSdkVideoRequestStatus.CANCELED
                    )
                    if (!validStates.contains(request.status)) {
                        throw TruvideoSdkVideoException("Video request in an invalid state")
                    }

                    // Validate data
                    val data = request.encodeData ?: run {
                        throw TruvideoSdkVideoException("Invalid request data")
                    }

                    // Update status
                    videoRequestRepository.tryChangeStatusToProcessing(id)

                    // Generate command
                    val command = generateEncodeVideosCommandUseCase(
                        data.videoPath,
                        data.resultPath,
                        data.receivedWidth,
                        data.receivedHeight,
                        data.videoCodec,
                        data.framesRate
                    )

                    // Execute
                    val commandId = ffmpegAdapter.executeAsync(command) { _, code, _ ->
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
                                    val exception = TruvideoSdkVideoException("Error encoding the video in merge process")
                                    videoRequestRepository.tryChangeStatusToError(
                                        id, exception.message
                                    )
                                    cont.resumeWith(Result.failure(exception))
                                }
                            }
                        }
                    }

                    // Update the command id
                    videoRequestRepository.tryUpdateCommandId(id, commandId)
                } catch (exception: Exception) {
                    exception.printStackTrace()

                    val message = exception.localizedMessage ?: "Unknown error"
                    videoRequestRepository.tryChangeStatusToError(id, message)

                    if (exception is TruvideoSdkVideoException) {
                        cont.resumeWith(Result.failure(exception))
                    } else {
                        cont.resumeWith(Result.failure(TruvideoSdkVideoException(message)))
                    }
                }
            }
        }
    }

    override fun process(
        id: String, callback: TruvideoSdkVideoCallback<Unit>
    ) {
        scope.launch {
            try {
                process(id)
                callback.onComplete(Unit)
            } catch (exception: Exception) {
                exception.printStackTrace()

                if (exception is TruvideoSdkVideoException) {
                    callback.onError(exception)
                } else {
                    callback.onError(
                        TruvideoSdkVideoException(
                            exception.localizedMessage ?: "Unknown error"
                        )
                    )
                }
            }
        }
    }

    override suspend fun cancel(id: String) {
        authAdapter.validateAuthentication()

        try {
            val request = videoRequestRepository.getById(id) ?: run {
                throw TruvideoSdkVideoException("Video request not found")
            }

            // Valid state
            if (request.status != TruvideoSdkVideoRequestStatus.PROCESSING) {
                throw TruvideoSdkVideoException("The request can't be cancelled")
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

            if (exception is TruvideoSdkVideoException) {
                throw exception
            } else {
                throw TruvideoSdkVideoException(exception.localizedMessage ?: "Unknown error")
            }
        }
    }

    override fun cancel(
        id: String, callback: TruvideoSdkVideoCallback<Unit>
    ) {
        scope.launch {
            try {
                cancel(id)
                callback.onComplete(Unit)
            } catch (exception: Exception) {
                exception.printStackTrace()

                if (exception is TruvideoSdkVideoException) {
                    callback.onError(exception)
                } else {
                    callback.onError(
                        TruvideoSdkVideoException(
                            exception.localizedMessage ?: "Unknown error"
                        )
                    )
                }
            }
        }
    }

    override suspend fun delete(id: String) {
        authAdapter.validateAuthentication()

        videoRequestRepository.delete(id)
    }

    override fun delete(id: String, callback: TruvideoSdkVideoCallback<Unit>) {
        scope.launch {
            try {
                delete(id)
                callback.onComplete(Unit)
            } catch (exception: Exception) {
                exception.printStackTrace()

                if (exception is TruvideoSdkVideoException) {
                    callback.onError(exception)
                } else {
                    callback.onError(
                        TruvideoSdkVideoException(
                            exception.localizedMessage ?: "Unknown error"
                        )
                    )
                }
            }
        }
    }
}