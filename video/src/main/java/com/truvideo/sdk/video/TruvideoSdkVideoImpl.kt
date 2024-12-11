package com.truvideo.sdk.video

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.truvideo.sdk.video.adapters.TruvideoSdkVideoVersionPropertiesAdapterImpl
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideo
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoAuthAdapter
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoCallback
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoLogAdapter
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoRequestEngine
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoRequestRepository
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile
import com.truvideo.sdk.video.model.TruvideoSdkVideoFileDescriptor
import com.truvideo.sdk.video.model.TruvideoSdkVideoInformation
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequest
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestStatus
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestType
import com.truvideo.sdk.video.model.TruvideoSdkVideoRotation
import com.truvideo.sdk.video.model.TruvideoSdkVideoSdkFeature
import com.truvideo.sdk.video.usecases.ClearNoiseUseCase
import com.truvideo.sdk.video.usecases.CompareVideosUseCase
import com.truvideo.sdk.video.usecases.CreateVideoThumbnailUseCase
import com.truvideo.sdk.video.usecases.EditVideoUseCase
import com.truvideo.sdk.video.usecases.GetVideoInfoUseCase
import com.truvideo.sdk.video.video_request_builder.TruvideoSdkVideoConcatBuilder
import com.truvideo.sdk.video.video_request_builder.TruvideoSdkVideoEncodeBuilder
import com.truvideo.sdk.video.video_request_builder.TruvideoSdkVideoMergeBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import truvideo.sdk.common.TruvideoSdkContextProvider
import truvideo.sdk.common.exceptions.TruvideoSdkException
import truvideo.sdk.common.model.TruvideoSdkLogSeverity
import java.io.File
import kotlin.time.DurationUnit
import kotlin.time.toDuration

internal class TruvideoSdkVideoImpl(
    private val context: Context,
    private val logAdapter: TruvideoSdkVideoLogAdapter,
    private val authAdapter: TruvideoSdkVideoAuthAdapter,
    private val getVideoInfoUseCase: GetVideoInfoUseCase,
    private val compareVideosUseCase: CompareVideosUseCase,
    private val editVideoUseCase: EditVideoUseCase,
    private val clearVideoAudioNoiseUseCase: ClearNoiseUseCase,
    private val createVideoThumbnailUseCase: CreateVideoThumbnailUseCase,
    private val videoRequestRepository: TruvideoSdkVideoRequestRepository,
    private val mergeEngine: TruvideoSdkVideoRequestEngine,
    private val concatEngine: TruvideoSdkVideoRequestEngine,
    private val encodeEngine: TruvideoSdkVideoRequestEngine,
    versionPropertiesAdapter: TruvideoSdkVideoVersionPropertiesAdapterImpl
) : TruvideoSdkVideo {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val moduleVersion = versionPropertiesAdapter.readProperty("versionName") ?: "Unknown"

    init {
        TruvideoSdkContextProvider.instance.init(context);

        logAdapter.addLog(
            eventName = "event_video_init",
            message = "Init video module",
            severity = TruvideoSdkLogSeverity.INFO,
        )

        scope.launch { videoRequestRepository.cancelAllProcessing() }
    }

    override suspend fun getAllRequests(status: TruvideoSdkVideoRequestStatus?): List<TruvideoSdkVideoRequest> {
        logAdapter.addLog(
            eventName = "event_video_request_get_all",
            message = "Get all requests called with status: $status",
            severity = TruvideoSdkLogSeverity.INFO
        )

        authAdapter.validateAuthentication()

        try {
            val data = videoRequestRepository.getAll(status = status)
            data.forEach {
                when (it.type) {
                    TruvideoSdkVideoRequestType.MERGE -> it.setEngine(mergeEngine)
                    TruvideoSdkVideoRequestType.CONCAT -> it.setEngine(concatEngine)
                    TruvideoSdkVideoRequestType.ENCODE -> it.setEngine(encodeEngine)
                }
            }

            return data
        } catch (exception: Exception) {
            exception.printStackTrace()
            logAdapter.addLog(
                eventName = "event_video_request_get_all",
                message = "Fail to get all requests called with status: $status. ${exception.localizedMessage}",
                severity = TruvideoSdkLogSeverity.ERROR
            )

            throw if (exception is TruvideoSdkException) {
                exception
            } else {
                TruvideoSdkException("Unknown error")
            }
        }
    }

    override fun getAllRequests(status: TruvideoSdkVideoRequestStatus?, callback: TruvideoSdkVideoCallback<List<TruvideoSdkVideoRequest>>) {
        scope.launch {
            try {
                val requests = getAllRequests(status)
                callback.onComplete(requests)
            } catch (exception: Exception) {
                if (exception is TruvideoSdkException) {
                    callback.onError(exception)
                } else {
                    callback.onError(TruvideoSdkException("Unknown error"))
                }
            }
        }
    }

    override fun streamAllRequests(status: TruvideoSdkVideoRequestStatus?): LiveData<List<TruvideoSdkVideoRequest>> {
        logAdapter.addLog(
            eventName = "event_video_request_stream_all",
            message = "Stream all requests called with status: $status",
            severity = TruvideoSdkLogSeverity.INFO
        )

        authAdapter.validateAuthentication()

        try {
            return videoRequestRepository.streamAll(status = status)
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
        } catch (exception: Exception) {
            exception.printStackTrace()
            logAdapter.addLog(
                eventName = "event_video_request_stream_all",
                message = "Fail to stream all requests called with status: $status. ${exception.localizedMessage}",
                severity = TruvideoSdkLogSeverity.ERROR
            )

            throw if (exception is TruvideoSdkException) {
                exception
            } else {
                TruvideoSdkException("Unknown error")
            }
        }
    }

    override fun streamRequestById(id: String): LiveData<TruvideoSdkVideoRequest?> {
        logAdapter.addLog(
            eventName = "event_video_request_stream_by_id",
            message = "Stream request by id called with id: $id",
            severity = TruvideoSdkLogSeverity.INFO
        )

        authAdapter.validateAuthentication()

        try {
            return videoRequestRepository.streamById(id).map {
                when (it?.type) {
                    TruvideoSdkVideoRequestType.MERGE -> it.setEngine(mergeEngine)
                    TruvideoSdkVideoRequestType.CONCAT -> it.setEngine(concatEngine)
                    TruvideoSdkVideoRequestType.ENCODE -> it.setEngine(encodeEngine)
                    null -> {}
                }

                it
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            logAdapter.addLog(
                eventName = "event_video_request_stream_by_id",
                message = "Fail to stream request by id called with id: $id. ${exception.localizedMessage}",
                severity = TruvideoSdkLogSeverity.ERROR
            )

            throw if (exception is TruvideoSdkException) {
                exception
            } else {
                TruvideoSdkException("Unknown error")
            }
        }

    }

    override suspend fun getRequestById(id: String): TruvideoSdkVideoRequest? {
        logAdapter.addLog(
            eventName = "event_video_request_get_by_id",
            message = "Get request by id called with id: $id",
            severity = TruvideoSdkLogSeverity.INFO
        )

        authAdapter.validateAuthentication()

        try {
            val data = videoRequestRepository.getById(id)
            when (data?.type) {
                TruvideoSdkVideoRequestType.MERGE -> data.setEngine(mergeEngine)
                TruvideoSdkVideoRequestType.CONCAT -> data.setEngine(concatEngine)
                TruvideoSdkVideoRequestType.ENCODE -> data.setEngine(encodeEngine)
                null -> {}
            }

            return data
        } catch (exception: Exception) {
            exception.printStackTrace()
            logAdapter.addLog(
                eventName = "event_video_request_get_by_id",
                message = "Fail to get request by id called with id: $id. ${exception.localizedMessage}",
                severity = TruvideoSdkLogSeverity.ERROR
            )

            throw if (exception is TruvideoSdkException) {
                exception
            } else {
                TruvideoSdkException("Unknown error")
            }
        }
    }

    override fun getRequestById(id: String, callback: TruvideoSdkVideoCallback<TruvideoSdkVideoRequest?>) {
        scope.launch {
            try {
                val requests = getRequestById(id)
                callback.onComplete(requests)
            } catch (exception: Exception) {
                if (exception is TruvideoSdkException) {
                    callback.onError(exception)
                } else {
                    callback.onError(TruvideoSdkException("Unknown error"))
                }
            }
        }
    }

    override suspend fun clearNoise(
        input: TruvideoSdkVideoFile,
        output: TruvideoSdkVideoFileDescriptor,
    ): String {
        val inputPath = input.getPath(context)
        val outputPath = output.getPath(context, File(inputPath).extension)

        logAdapter.addLog(
            eventName = "event_video_clear_noise",
            message = "Clear noise called with inputPath: $inputPath, outputPath: $outputPath",
            severity = TruvideoSdkLogSeverity.INFO
        )

        authAdapter.validateAuthentication()
        authAdapter.verifyFeatureAvailable(TruvideoSdkVideoSdkFeature.NOISE_CANCELLING)

        try {
            clearVideoAudioNoiseUseCase(
                input = input,
                output = output
            )
            return outputPath
        } catch (exception: Exception) {
            exception.printStackTrace()
            logAdapter.addLog(
                eventName = "event_video_clear_noise",
                message = "Error clearing noise. $inputPath. ${exception.localizedMessage}",
                severity = TruvideoSdkLogSeverity.ERROR
            )

            throw if (exception is TruvideoSdkException) {
                exception
            } else {
                TruvideoSdkException("Unknown error")
            }
        }
    }

    override fun clearNoise(
        input: TruvideoSdkVideoFile,
        output: TruvideoSdkVideoFileDescriptor,
        callback: TruvideoSdkVideoCallback<String>
    ) {
        scope.launch {
            try {
                val result = clearNoise(
                    input = input,
                    output = output,
                )
                callback.onComplete(result)
            } catch (exception: Exception) {
                if (exception is TruvideoSdkException) {
                    callback.onError(exception)
                } else {
                    callback.onError(TruvideoSdkException("Unknown error"))
                }
            }
        }
    }

    override suspend fun compare(
        input: List<TruvideoSdkVideoFile>,
    ): Boolean {
        val inputPaths = input.map { it.getPath(context) }
        logAdapter.addLog(
            eventName = "event_video_compare",
            message = "Compare called with input paths: $inputPaths",
            severity = TruvideoSdkLogSeverity.INFO
        )

        authAdapter.validateAuthentication()

        try {
            return compareVideosUseCase(input)
        } catch (exception: Exception) {
            exception.printStackTrace()
            logAdapter.addLog(
                eventName = "event_video_get_info",
                message = "Error comparing videos. ${exception.localizedMessage}",
                severity = TruvideoSdkLogSeverity.ERROR
            )

            if (exception is TruvideoSdkException) {
                throw exception
            } else {
                throw TruvideoSdkException("Unknown error")
            }
        }
    }

    override fun compare(
        input: List<TruvideoSdkVideoFile>,
        callback: TruvideoSdkVideoCallback<Boolean>
    ) {
        scope.launch {
            try {
                val areReady = compare(input)
                callback.onComplete(areReady)
            } catch (exception: Exception) {
                if (exception is TruvideoSdkException) {
                    callback.onError(exception)
                } else {
                    callback.onError(TruvideoSdkException("Unknown error"))
                }
            }
        }
    }

    override suspend fun getInfo(
        input: TruvideoSdkVideoFile,
    ): TruvideoSdkVideoInformation {
        val inputPath = input.getPath(context)
        logAdapter.addLog(
            eventName = "event_video_get_info",
            message = "Get info called with input path: $inputPath",
            severity = TruvideoSdkLogSeverity.INFO
        )

        authAdapter.validateAuthentication()

        try {
            return getVideoInfoUseCase(input)
        } catch (exception: Exception) {
            exception.printStackTrace()
            logAdapter.addLog(
                eventName = "event_video_get_info",
                message = "Error getting video info: $inputPath. ${exception.localizedMessage}",
                severity = TruvideoSdkLogSeverity.ERROR
            )

            if (exception is TruvideoSdkException) {
                throw exception
            } else {
                throw TruvideoSdkException("Unknown error")
            }
        }
    }

    override fun getInfo(
        input: TruvideoSdkVideoFile,
        callback: TruvideoSdkVideoCallback<TruvideoSdkVideoInformation>
    ) {
        scope.launch {
            try {
                val result = getInfo(input)
                callback.onComplete(result)
            } catch (exception: Exception) {
                if (exception is TruvideoSdkException) {
                    callback.onError(exception)
                } else {
                    callback.onError(TruvideoSdkException("Unknown error"))
                }
            }
        }
    }

    override suspend fun createThumbnail(
        input: TruvideoSdkVideoFile,
        output: TruvideoSdkVideoFileDescriptor,
        position: Long,
        width: Int?,
        height: Int?,
        precise: Boolean
    ): String {
        val inputPath = input.getPath(context)

        logAdapter.addLog(
            eventName = "event_video_create_thumbnail",
            message = "Create thumbnail called with input path: $inputPath",
            severity = TruvideoSdkLogSeverity.INFO
        )
        authAdapter.validateAuthentication()

        try {
            return createVideoThumbnailUseCase(
                input = input,
                output = output,
                position = position.toDuration(DurationUnit.MILLISECONDS),
                width = width,
                height = height,
                precise = precise
            )
        } catch (exception: Exception) {
            exception.printStackTrace()
            logAdapter.addLog(
                eventName = "event_video_create_thumbnail",
                message = "Error creating thumbnail: $inputPath. ${exception.localizedMessage}",
                severity = TruvideoSdkLogSeverity.ERROR
            )

            if (exception is TruvideoSdkException) {
                throw exception
            } else {
                throw TruvideoSdkException("Unknown error")
            }
        }
    }

    override fun createThumbnail(
        input: TruvideoSdkVideoFile,
        output: TruvideoSdkVideoFileDescriptor,
        position: Long,
        width: Int?,
        height: Int?,
        precise: Boolean,
        callback: TruvideoSdkVideoCallback<String>
    ) {
        scope.launch {
            try {
                val result = createThumbnail(
                    input = input,
                    output = output,
                    position = position,
                    width = width,
                    height = height,
                    precise = precise
                )
                callback.onComplete(result)
            } catch (exception: Exception) {
                if (exception is TruvideoSdkException) {
                    callback.onError(exception)
                } else {
                    callback.onError(TruvideoSdkException("Unknown error"))
                }
            }
        }
    }


    override suspend fun edit(
        input: TruvideoSdkVideoFile,
        output: TruvideoSdkVideoFileDescriptor,
        start: Long?,
        end: Long?,
        volume: Float,
        rotation: TruvideoSdkVideoRotation?
    ): String {
        val inputPath = input.getPath(context)
        logAdapter.addLog(
            eventName = "event_video_edit",
            message = "Edit called with input path: $inputPath",
            severity = TruvideoSdkLogSeverity.INFO
        )
        authAdapter.validateAuthentication()

        try {
            return editVideoUseCase(
                input = input,
                output = output,
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

            if (exception is TruvideoSdkException) {
                throw exception
            } else {
                throw TruvideoSdkException("Unknown error")
            }
        }
    }

    override fun edit(
        input: TruvideoSdkVideoFile,
        output: TruvideoSdkVideoFileDescriptor,
        start: Long?,
        end: Long?,
        volume: Float,
        rotation: TruvideoSdkVideoRotation?,
        callback: TruvideoSdkVideoCallback<String>
    ) {
        scope.launch {
            try {
                val result = edit(
                    input = input,
                    output = output,
                    start = start,
                    end = end,
                    volume = volume,
                    rotation = rotation
                )
                callback.onComplete(result)
            } catch (exception: Exception) {
                if (exception is TruvideoSdkException) {
                    callback.onError(exception)
                } else {
                    callback.onError(TruvideoSdkException("Unknown error"))
                }
            }
        }
    }

    override fun MergeBuilder(
        input: List<TruvideoSdkVideoFile>,
        output: TruvideoSdkVideoFileDescriptor,
    ): TruvideoSdkVideoMergeBuilder {
        logAdapter.addLog(
            eventName = "event_video_merge_build_create",
            message = "Building merge builder",
            severity = TruvideoSdkLogSeverity.INFO
        )

        val builder = TruvideoSdkVideoMergeBuilder(
            input = input,
            output = output
        )

        builder.context = context
        builder.engine = mergeEngine
        builder.repository = videoRequestRepository
        return builder
    }

    override fun ConcatBuilder(
        input: List<TruvideoSdkVideoFile>,
        output: TruvideoSdkVideoFileDescriptor,
    ): TruvideoSdkVideoConcatBuilder {
        logAdapter.addLog(
            eventName = "event_video_concat_build_create",
            message = "Building concat builder",
            severity = TruvideoSdkLogSeverity.INFO
        )

        val builder = TruvideoSdkVideoConcatBuilder(
            input = input,
            output = output
        )
        builder.context = context
        builder.engine = concatEngine
        builder.repository = videoRequestRepository
        return builder
    }

    override fun EncodeBuilder(
        input: TruvideoSdkVideoFile,
        output: TruvideoSdkVideoFileDescriptor,
    ): TruvideoSdkVideoEncodeBuilder {
        logAdapter.addLog(
            eventName = "event_video_encode_build_create",
            message = "Building encode builder",
            severity = TruvideoSdkLogSeverity.INFO
        )

        val builder = TruvideoSdkVideoEncodeBuilder(
            input = input,
            output = output
        )
        builder.context = context
        builder.engine = encodeEngine
        builder.repository = videoRequestRepository
        return builder
    }

    override val environment: String
        get() = BuildConfig.FLAVOR

    override val version: String
        get() = moduleVersion
}