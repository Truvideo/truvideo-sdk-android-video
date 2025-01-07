package com.truvideo.sdk.video.engines

import android.util.Log
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoAuthAdapter
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoCallback
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoFFmpegAdapter
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoRequestEngine
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoRequestRepository
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile
import com.truvideo.sdk.video.model.TruvideoSdkVideoFileDescriptor
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequest
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestStatus
import com.truvideo.sdk.video.usecases.GetVideoInfoUseCase
import com.truvideo.sdk.video.usecases.MergeVideosUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import truvideo.sdk.common.exceptions.TruvideoSdkException
import kotlin.coroutines.suspendCoroutine

internal class TruvideoSdkMergeVideoRequestEngineImpl(
    private val videoRequestRepository: TruvideoSdkVideoRequestRepository,
    private val authAdapter: TruvideoSdkVideoAuthAdapter,
    private val ffmpegAdapter: TruvideoSdkVideoFFmpegAdapter,
    private val mergeVideosUseCase: MergeVideosUseCase,
    private val getVideoInfoUseCase: GetVideoInfoUseCase
) : TruvideoSdkVideoRequestEngine {

    private val scope = CoroutineScope(Dispatchers.IO)

    override suspend fun process(id: String): String {
        authAdapter.validateAuthentication()

        val mutex = Mutex()

        return suspendCoroutine { cont ->
            scope.launch {
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
                    val data = request.mergeData ?: throw TruvideoSdkException("Invalid request data")

                    // Update status
                    videoRequestRepository.tryChangeStatusToProcessing(id)

                    val videoInfo = data.inputPaths.map { getVideoInfoUseCase(TruvideoSdkVideoFile.custom(it)) }.toList()
                    val totalDuration = videoInfo.sumOf { it.durationMillis }

                    // Generate command
                    mergeVideosUseCase.mergeAsync(
                        input = data.inputPaths.map { TruvideoSdkVideoFile.custom(it) },
                        output = TruvideoSdkVideoFileDescriptor.custom(data.outputPath),
                        width = data.width,
                        height = data.height,
                        frameRate = data.framesRate,
                        videoTracks = data.videoTracks,
                        audioTracks = data.audioTracks,
                        printLogs = true,
                        onRequestCreated = { commandId ->
                            scope.launch {
                                mutex.withLock {
                                    videoRequestRepository.tryUpdateCommandId(id, commandId)
                                }
                            }
                        },
                        progressCallback = {
                            scope.launch {
                                mutex.withLock {
                                    val progress: Double = it.toDouble() / totalDuration.toDouble()
                                    videoRequestRepository.tryUpdateProgress(id, progress.toFloat().coerceIn(0f, 1f))
                                }
                            }
                        },
                        callback = { path ->
                            scope.launch {
                                mutex.withLock {
                                    Log.d("TruvideoSdkVideo", "Request completed")
                                    videoRequestRepository.tryChangeStatusToCompleted(id, path)
                                    cont.resumeWith(Result.success(path))
                                }
                            }
                        },
                        callbackCanceled = {
                            scope.launch {
                                mutex.withLock {
                                    Log.d("TruvideoSdkVideo", "Request completed")
                                    videoRequestRepository.tryChangeStatusToCanceled(id)
                                    cont.resumeWith(Result.failure(TruvideoSdkException("Request canceled")))
                                }
                            }
                        },
                        callbackError = { exception ->
                            scope.launch {
                                mutex.withLock {
                                    Log.d("TruvideoSdkVideo", "Request error. Error: ${exception.localizedMessage}")
                                    val message = exception.localizedMessage ?: "Unknown error"
                                    videoRequestRepository.tryChangeStatusToError(id, message)
                                    cont.resumeWith(Result.failure(exception))
                                }
                            }
                        }
                    )
                } catch (exception: Exception) {
                    exception.printStackTrace()

                    if (exception is TruvideoSdkException) {
                        cont.resumeWith(Result.failure(exception))
                    } else {
                        cont.resumeWith(Result.failure(TruvideoSdkException("Unknown error")))
                    }
                }
            }
        }
    }

    override fun process(
        id: String,
        callback: TruvideoSdkVideoCallback<String>
    ) {
        scope.launch {
            try {
                val result = process(id)
                callback.onComplete(result)
            } catch (exception: Exception) {
                exception.printStackTrace()

                if (exception is TruvideoSdkException) {
                    callback.onError(exception)
                } else {
                    callback.onError(TruvideoSdkException("Unknown error"))
                }
            }
        }
    }

    override suspend fun cancel(id: String) {
        authAdapter.validateAuthentication()

        try {
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

            if (exception is TruvideoSdkException) {
                throw exception
            } else {
                throw TruvideoSdkException("Unknown error")
            }
        }
    }

    override fun cancel(
        id: String,
        callback: TruvideoSdkVideoCallback<Unit>
    ) {
        scope.launch {
            try {
                cancel(id)
                callback.onComplete(Unit)
            } catch (exception: Exception) {
                exception.printStackTrace()

                if (exception is TruvideoSdkException) {
                    callback.onError(exception)
                } else {
                    callback.onError(TruvideoSdkException("Unknown error"))
                }
            }
        }
    }

    override suspend fun delete(id: String) {
        authAdapter.validateAuthentication()

        try {
            val request = videoRequestRepository.getById(id) ?: throw TruvideoSdkException("Video request not found")

            // Valid state
            if (request.status == TruvideoSdkVideoRequestStatus.PROCESSING) {
                throw TruvideoSdkException("The request can't be cancelled")
            }

            // Cancel ffmpeg command
            val commandId = request.commandId
            if (commandId != null) {
                ffmpegAdapter.cancel(commandId)
            }

            // delete
            videoRequestRepository.delete(id)
        } catch (exception: Exception) {
            exception.printStackTrace()

            if (exception is TruvideoSdkException) {
                throw exception
            } else {
                throw TruvideoSdkException("Unknown error")
            }
        }
    }

    override fun delete(id: String, callback: TruvideoSdkVideoCallback<Unit>) {
        scope.launch {
            try {
                delete(id)
                callback.onComplete(Unit)
            } catch (exception: Exception) {
                exception.printStackTrace()

                if (exception is TruvideoSdkException) {
                    callback.onError(exception)
                } else {
                    callback.onError(TruvideoSdkException("Unknown error"))
                }
            }
        }
    }

    override suspend fun update(request: TruvideoSdkVideoRequest) {
        try {
            videoRequestRepository.update(request)
        } catch (exception: Exception) {
            exception.printStackTrace()

            if (exception is TruvideoSdkException) {
                throw exception
            } else {
                throw TruvideoSdkException("Unknown error")
            }
        }
    }

    override fun update(request: TruvideoSdkVideoRequest, callback: TruvideoSdkVideoCallback<Unit>) {
        scope.launch {
            try {
                update(request)
                callback.onComplete(Unit)
            } catch (exception: Exception) {
                exception.printStackTrace()

                if (exception is TruvideoSdkException) {
                    callback.onError(exception)
                } else {
                    callback.onError(TruvideoSdkException("Unknown error"))
                }
            }
        }
    }
}