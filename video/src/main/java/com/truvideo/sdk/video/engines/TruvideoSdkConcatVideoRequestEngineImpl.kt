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
import com.truvideo.sdk.video.usecases.CompareVideosUseCase
import com.truvideo.sdk.video.usecases.ConcatVideosUseCase
import com.truvideo.sdk.video.usecases.GetVideoInfoUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import truvideo.sdk.common.exceptions.TruvideoSdkException

internal class TruvideoSdkConcatVideoRequestEngineImpl(
    private val concatVideosUseCase: ConcatVideosUseCase,
    private val getVideoInfoUseCase: GetVideoInfoUseCase,
    private val videoRequestRepository: TruvideoSdkVideoRequestRepository,
    private val authAdapter: TruvideoSdkVideoAuthAdapter,
    private val compareVideosUseCase: CompareVideosUseCase,
    private val ffmpegAdapter: TruvideoSdkVideoFFmpegAdapter
) : TruvideoSdkVideoRequestEngine {

    private val scope = CoroutineScope(Dispatchers.IO)

    override suspend fun process(id: String): String {
        authAdapter.validateAuthentication()

        val mutex = Mutex()

        return suspendCancellableCoroutine { cont ->
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
                    if (!validStates.contains(request.status)) throw TruvideoSdkException("Video request in an invalid state")

                    // Validate data
                    val data = request.concatData ?: throw TruvideoSdkException("Invalid request data")

                    // Update status
                    videoRequestRepository.tryChangeStatusToProcessing(id)

                    // Compare videos
                    val compareResult = compareVideosUseCase(data.inputPaths.map { TruvideoSdkVideoFile.custom(it) })
                    if (!compareResult) throw TruvideoSdkException("The videos are not valid for concatenation")

                    val videoInfo = data.inputPaths.map { getVideoInfoUseCase(TruvideoSdkVideoFile.custom(it)) }.toList()
                    val totalDuration = videoInfo.sumOf { it.durationMillis }

                    // Generate command
                    concatVideosUseCase.concat(
                        input = data.inputPaths.map { TruvideoSdkVideoFile.custom(it) },
                        output = TruvideoSdkVideoFileDescriptor.custom(data.outputPath),
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
                                    Log.d("TruvideoSdkVideo", "Request canceled")
                                    videoRequestRepository.tryChangeStatusToCanceled(id)
                                    cont.resumeWith(Result.failure(TruvideoSdkException("Request canceled")))
                                }
                            }
                        },
                        callbackError = { exception ->
                            scope.launch {
                                mutex.withLock {
                                    Log.d("TruvideoSdkVideo", "Request error. ${exception.localizedMessage}")
                                    val message = exception.localizedMessage ?: "Unknown error"
                                    videoRequestRepository.tryChangeStatusToError(id, message)
                                    cont.resumeWith(Result.failure(exception))
                                }
                            }
                        }
                    )
                } catch (exception: Exception) {
                    exception.printStackTrace()

                    val message = if (exception is TruvideoSdkException) exception.localizedMessage ?: "" else "Unknown error"
                    videoRequestRepository.tryChangeStatusToError(id, message)

                    if (exception is TruvideoSdkException) {
                        cont.resumeWith(Result.failure(exception))
                    } else {
                        cont.resumeWith(Result.failure(TruvideoSdkException(message)))
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