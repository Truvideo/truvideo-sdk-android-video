package com.truvideo.sdk.video

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideo
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoAuthAdapter
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoCallback
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoLogAdapter
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoRequestEngine
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoRequestRepository
import com.truvideo.sdk.video.model.SdkFeature
import com.truvideo.sdk.video.model.TruvideoSdkVideoException
import com.truvideo.sdk.video.model.TruvideoSdkVideoInformation
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequest
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestStatus
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestType
import com.truvideo.sdk.video.model.TruvideoSdkVideoRotation
import com.truvideo.sdk.video.usecases.ClearVideoAudioNoiseUseCase
import com.truvideo.sdk.video.usecases.CompareVideosUseCase
import com.truvideo.sdk.video.usecases.CreateVideoThumbnailUseCase
import com.truvideo.sdk.video.usecases.EditVideoUseCase
import com.truvideo.sdk.video.usecases.GetVideoInfoUseCase
import com.truvideo.sdk.video.usecases.OpenEditScreenUseCase
import com.truvideo.sdk.video.usecases.TruvideoSdkVideoEditScreen
import com.truvideo.sdk.video.video_request_builder.TruvideoSdkVideoConcatBuilder
import com.truvideo.sdk.video.video_request_builder.TruvideoSdkVideoEncodeBuilder
import com.truvideo.sdk.video.video_request_builder.TruvideoSdkVideoMergeBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import truvideo.sdk.common.TruvideoSdkContextProvider
import truvideo.sdk.common.model.TruvideoSdkLogSeverity
import kotlin.time.DurationUnit
import kotlin.time.toDuration

internal class TruvideoSdkVideoImpl(
    context: Context,
    private val logAdapter: TruvideoSdkVideoLogAdapter,
    private val authAdapter: TruvideoSdkVideoAuthAdapter,
    private val getVideoInfoUseCase: GetVideoInfoUseCase,
    private val compareVideosUseCase: CompareVideosUseCase,
    private val editVideoUseCase: EditVideoUseCase,
    private val openEditScreenUseCase: OpenEditScreenUseCase,
    private val clearVideoAudioNoiseUseCase: ClearVideoAudioNoiseUseCase,
    private val createVideoThumbnailUseCase: CreateVideoThumbnailUseCase,
    private val videoRequestRepository: TruvideoSdkVideoRequestRepository,
    private val mergeEngine: TruvideoSdkVideoRequestEngine,
    private val concatEngine: TruvideoSdkVideoRequestEngine,
    private val encodeEngine: TruvideoSdkVideoRequestEngine
) : TruvideoSdkVideo {

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        TruvideoSdkContextProvider.instance.init(context);

        logAdapter.addLog(
            eventName = "event_video_init",
            message = "Init video module",
            severity = TruvideoSdkLogSeverity.INFO,
        )

        scope.launch { videoRequestRepository.cancelAllProcessing() }
    }

    override fun initEditScreen(activity: ComponentActivity): TruvideoSdkVideoEditScreen {
        logAdapter.addLog(
            eventName = "event_video_edit_screen_init",
            message = "Init edit screen called",
            severity = TruvideoSdkLogSeverity.INFO,
        )

        return openEditScreenUseCase.init(activity)
    }

    override suspend fun getAllRequests(status: TruvideoSdkVideoRequestStatus?): List<TruvideoSdkVideoRequest> {
        logAdapter.addLog(
            eventName = "event_video_request_get_all",
            message = "Get all requests called with status: $status",
            severity = TruvideoSdkLogSeverity.INFO
        )

        authAdapter.validateAuthentication()

        val data = videoRequestRepository.getAll(TruvideoSdkVideoRequestStatus.PROCESSING)
        data.forEach {
            when (it.type) {
                TruvideoSdkVideoRequestType.MERGE -> it.setEngine(mergeEngine)
                TruvideoSdkVideoRequestType.CONCAT -> it.setEngine(concatEngine)
                TruvideoSdkVideoRequestType.ENCODE -> it.setEngine(encodeEngine)
            }
        }

        return data
    }

    override fun streamAllRequests(status: TruvideoSdkVideoRequestStatus?): LiveData<List<TruvideoSdkVideoRequest>> {
        logAdapter.addLog(
            eventName = "event_video_request_stream_all",
            message = "Stream all requests called with status: $status",
            severity = TruvideoSdkLogSeverity.INFO
        )

        authAdapter.validateAuthentication()

        return videoRequestRepository.streamAll(
            status = status,
            mergeEngine = mergeEngine,
            concatEngine = concatEngine,
            encodeEngine = encodeEngine
        )
            .map(fun(data: @JvmSuppressWildcards List<TruvideoSdkVideoRequest>): @JvmSuppressWildcards List<TruvideoSdkVideoRequest> {
                data.forEach {
                    when (it.type) {
                        TruvideoSdkVideoRequestType.MERGE -> it.setEngine(mergeEngine)
                        TruvideoSdkVideoRequestType.CONCAT -> it.setEngine(concatEngine)
                        TruvideoSdkVideoRequestType.ENCODE -> it.setEngine(encodeEngine)
                    }
                }

                return data
            })
    }

    override suspend fun clearNoise(
        videoPath: String,
        resultPath: String
    ) {
        logAdapter.addLog(
            eventName = "event_video_clear_noise",
            message = "Clear noise called with videoPath: $videoPath, resultPath: $resultPath",
            severity = TruvideoSdkLogSeverity.INFO
        )

        authAdapter.validateAuthentication()

        try {
            authAdapter.verifyFeatureAvailable(SdkFeature.NOISE_CANCELLING)
            clearVideoAudioNoiseUseCase(
                videoPath = videoPath,
                resultPath = resultPath
            )
        } catch (exception: Exception) {
            exception.printStackTrace()

            logAdapter.addLog(
                eventName = "event_video_clear_noise",
                message = "Error clearing noise. $videoPath. ${exception.localizedMessage}",
                severity = TruvideoSdkLogSeverity.ERROR
            )

            if (exception is TruvideoSdkVideoException) {
                throw exception
            } else {
                throw TruvideoSdkVideoException(exception.localizedMessage ?: "")
            }
        }
    }

    override fun clearNoise(
        videoPath: String,
        resultPath: String,
        callback: TruvideoSdkVideoCallback<String>
    ) {
        scope.launch {
            try {
                clearNoise(
                    videoPath = videoPath,
                    resultPath = resultPath,
                )
                callback.onComplete(resultPath)
            } catch (exception: Exception) {
                exception.printStackTrace()

                if (exception is TruvideoSdkVideoException) {
                    throw exception
                } else {
                    throw TruvideoSdkVideoException(exception.localizedMessage ?: "Unknown error")
                }

            }
        }
    }


    override suspend fun compare(videoPaths: List<String>): Boolean {
        logAdapter.addLog(
            eventName = "event_video_compare",
            message = "Compare called with videoPaths: $videoPaths",
            severity = TruvideoSdkLogSeverity.INFO
        )

        authAdapter.validateAuthentication()

        try {
            return compareVideosUseCase(videoPaths)
        } catch (exception: Exception) {
            exception.printStackTrace()

            logAdapter.addLog(
                eventName = "event_video_get_info",
                message = "Error comparing videos. ${exception.localizedMessage}",
                severity = TruvideoSdkLogSeverity.ERROR
            )

            if (exception is TruvideoSdkVideoException) {
                throw exception
            } else {
                throw TruvideoSdkVideoException(exception.localizedMessage ?: "Unknown error")
            }
        }
    }

    override fun compare(
        videoPaths: List<String>,
        callback: TruvideoSdkVideoCallback<Boolean>
    ) {
        scope.launch {
            try {
                val areReady = compare(videoPaths)
                callback.onComplete(areReady)
            } catch (exception: Exception) {
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

    override suspend fun getInfo(videoPath: String): TruvideoSdkVideoInformation {
        logAdapter.addLog(
            eventName = "event_video_get_info",
            message = "Get info called with videoPath: $videoPath",
            severity = TruvideoSdkLogSeverity.INFO
        )

        authAdapter.validateAuthentication()

        try {
            return getVideoInfoUseCase(videoPath)
        } catch (exception: Exception) {
            exception.printStackTrace()

            logAdapter.addLog(
                eventName = "event_video_get_info",
                message = "Error getting video info: $videoPath. ${exception.localizedMessage}",
                severity = TruvideoSdkLogSeverity.ERROR
            )

            if (exception is TruvideoSdkVideoException) {
                throw exception
            } else {
                throw TruvideoSdkVideoException(exception.localizedMessage ?: "Unknown error")
            }
        }
    }

    override fun getInfo(
        videoPath: String,
        callback: TruvideoSdkVideoCallback<TruvideoSdkVideoInformation>
    ) {
        scope.launch {
            try {
                val result = getInfo(videoPath)
                callback.onComplete(result)
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

    override suspend fun createThumbnail(
        videoPath: String,
        resultPath: String,
        position: Long,
        width: Int?,
        height: Int?,
    ): String {
        logAdapter.addLog(
            eventName = "event_video_create_thumbnail",
            message = "Create thumbnail called with videoPath: $videoPath",
            severity = TruvideoSdkLogSeverity.INFO
        )
        authAdapter.validateAuthentication()

        try {
            return createVideoThumbnailUseCase(
                videoPath,
                resultPath,
                position.toDuration(DurationUnit.MILLISECONDS),
                width,
                height,
            )
        } catch (exception: Exception) {
            exception.printStackTrace()

            logAdapter.addLog(
                eventName = "event_video_create_thumbnail",
                message = "Error creating thumbnail: $videoPath. ${exception.localizedMessage}",
                severity = TruvideoSdkLogSeverity.ERROR
            )

            if (exception is TruvideoSdkVideoException) {
                throw exception
            } else {
                throw TruvideoSdkVideoException(exception.localizedMessage ?: "Unknown error")
            }
        }
    }

    override fun createThumbnail(
        videoPath: String,
        resultPath: String,
        position: Long,
        width: Int?,
        height: Int?,
        callback: TruvideoSdkVideoCallback<String>
    ) {
        scope.launch {
            try {
                val result = createThumbnail(
                    videoPath = videoPath,
                    resultPath = resultPath,
                    position = position,
                    width = width,
                    height = height
                )
                callback.onComplete(result)
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


    override suspend fun edit(
        videoPath: String,
        resultPath: String,
        start: Long?,
        end: Long?,
        volume: Float,
        rotation: TruvideoSdkVideoRotation?
    ): String {
        logAdapter.addLog(
            eventName = "event_video_edit",
            message = "Edit called with videoPath: $videoPath",
            severity = TruvideoSdkLogSeverity.INFO
        )
        authAdapter.validateAuthentication()

        try {
            return editVideoUseCase(
                videoPath = videoPath,
                resultPath = resultPath,
                startPosition = start,
                endPosition = end,
                rotation = rotation,
                volume = volume
            )
        } catch (exception: Exception) {
            exception.printStackTrace()

            logAdapter.addLog(
                eventName = "event_video_edit",
                message = "Error editing video. ${exception.localizedMessage}",
                severity = TruvideoSdkLogSeverity.ERROR
            )

            if (exception is TruvideoSdkVideoException) {
                throw exception
            } else {
                throw TruvideoSdkVideoException(exception.localizedMessage ?: "Unknown error")
            }
        }
    }

    override fun edit(
        videoPath: String,
        resultPath: String,
        start: Long?,
        end: Long?,
        volume: Float,
        rotation: TruvideoSdkVideoRotation?,
        callback: TruvideoSdkVideoCallback<String>
    ) {
        scope.launch {
            try {
                val result = edit(
                    videoPath = videoPath,
                    resultPath = resultPath,
                    start = start,
                    end = end,
                    volume = volume,
                    rotation = rotation
                )
                callback.onComplete(result)
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

    override fun MergeBuilder(
        videoPaths: List<String>, resultPath: String
    ): TruvideoSdkVideoMergeBuilder {
        logAdapter.addLog(
            eventName = "event_video_merge_build_create",
            message = "Building merge builder",
            severity = TruvideoSdkLogSeverity.INFO
        )

        return TruvideoSdkVideoMergeBuilder(
            videoPaths = videoPaths,
            resultPath = resultPath
        ).apply {
            engine = mergeEngine
            repository = videoRequestRepository
        }
    }

    override fun ConcatBuilder(
        videoPaths: List<String>,
        resultPath: String,
    ): TruvideoSdkVideoConcatBuilder {
        logAdapter.addLog(
            eventName = "event_video_concat_build_create",
            message = "Building concat builder",
            severity = TruvideoSdkLogSeverity.INFO
        )

        return TruvideoSdkVideoConcatBuilder(
            videoPaths = videoPaths,
            resultPath = resultPath
        ).apply {
            engine = concatEngine
            repository = videoRequestRepository
        }
    }

    override fun EncodeBuilder(
        videoPath: String,
        resultPath: String,
    ): TruvideoSdkVideoEncodeBuilder {
        logAdapter.addLog(
            eventName = "event_video_encode_build_create",
            message = "Building encode builder",
            severity = TruvideoSdkLogSeverity.INFO
        )

        return TruvideoSdkVideoEncodeBuilder(
            videoPath = videoPath,
            resultPath = resultPath
        ).apply {
            engine = encodeEngine
            repository = videoRequestRepository
        }
    }

    override val environment: String
        get() = BuildConfig.FLAVOR
}