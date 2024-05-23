package com.truvideo.sdk.video

import androidx.activity.ComponentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideo
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoAuthAdapter
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoCallback
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
import com.truvideo.sdk.video.video_request_builder.TruvideoSdkVideoConcatBuilder
import com.truvideo.sdk.video.video_request_builder.TruvideoSdkVideoEncodeBuilder
import com.truvideo.sdk.video.video_request_builder.TruvideoSdkVideoMergeBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.time.DurationUnit
import kotlin.time.toDuration

internal class TruvideoSdkVideoImpl(
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
    private val encodeEngine: TruvideoSdkVideoRequestEngine,
) : TruvideoSdkVideo {

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            val requests = videoRequestRepository.getAll(TruvideoSdkVideoRequestStatus.PROCESSING)
            requests.forEach {
                when (it.type) {
                    TruvideoSdkVideoRequestType.MERGE -> it.setEngine(mergeEngine)
                    TruvideoSdkVideoRequestType.CONCAT -> it.setEngine(concatEngine)
                    TruvideoSdkVideoRequestType.ENCODE -> it.setEngine(encodeEngine)
                }
            }
            requests.forEach { it.cancel() }
        }
    }

    override fun initEditor(activity: ComponentActivity) = openEditScreenUseCase.init(activity)

    override suspend fun getAllRequests(status: TruvideoSdkVideoRequestStatus?): List<TruvideoSdkVideoRequest> {
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
        authAdapter.validateAuthentication()

        return videoRequestRepository.streamAll(
            status,
            mergeEngine,
            concatEngine,
            encodeEngine
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
        authAdapter.validateAuthentication()

        try {
            authAdapter.verifyFeatureAvailable(SdkFeature.NOISE_CANCELLING)
            clearVideoAudioNoiseUseCase(
                videoPath, resultPath
            )
        } catch (exception: Exception) {
            exception.printStackTrace()
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
                    videoPath,
                    resultPath,
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
        authAdapter.validateAuthentication()

        try {
            return compareVideosUseCase(videoPaths)
        } catch (exception: Exception) {
            exception.printStackTrace()

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
                    callback.onError(TruvideoSdkVideoException(exception.localizedMessage ?: "Unknown error"))
                }
            }
        }
    }

    override suspend fun getInfo(videoPath: String): TruvideoSdkVideoInformation {
        authAdapter.validateAuthentication()

        try {
            return getVideoInfoUseCase(videoPath)
        } catch (exception: Exception) {
            exception.printStackTrace()

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
                    callback.onError(TruvideoSdkVideoException(exception.localizedMessage ?: "Unknown error"))
                }
            }
        }
    }

    override suspend fun createThumbnail(
        videoPath: String,
        resultPath: String,
        position: Long,
        width: Int?,
        height: Int?
    ): String {
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
                    videoPath,
                    resultPath,
                    position,
                    width,
                    height
                )
                callback.onComplete(result)
            } catch (exception: Exception) {
                exception.printStackTrace()

                if (exception is TruvideoSdkVideoException) {
                    callback.onError(exception)
                } else {
                    callback.onError(TruvideoSdkVideoException(exception.localizedMessage ?: "Unknown error"))
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
        authAdapter.validateAuthentication()

        try {
            return editVideoUseCase(
                videoPath,
                resultPath,
                start,
                end,
                rotation,
                volume
            )
        } catch (exception: Exception) {
            exception.printStackTrace()

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
                    videoPath,
                    resultPath,
                    start,
                    end,
                    volume,
                    rotation
                )
                callback.onComplete(result)
            } catch (exception: Exception) {
                exception.printStackTrace()
                if (exception is TruvideoSdkVideoException) {
                    callback.onError(exception)
                } else {
                    callback.onError(TruvideoSdkVideoException(exception.localizedMessage ?: "Unknown error"))
                }
            }
        }
    }

    override fun MergeBuilder(
        videoPaths: List<String>,
        resultPath: String
    ): TruvideoSdkVideoMergeBuilder = TruvideoSdkVideoMergeBuilder(
        engine = mergeEngine,
        repository = videoRequestRepository,
        videoPaths = videoPaths,
        resultPath = resultPath
    )

    override fun ConcatBuilder(
        videoPaths: List<String>,
        resultPath: String,
    ): TruvideoSdkVideoConcatBuilder = TruvideoSdkVideoConcatBuilder(
        engine = concatEngine,
        repository = videoRequestRepository,
        videoPaths = videoPaths,
        resultPath = resultPath
    )

    override fun EncodeBuilder(
        videoPath: String,
        resultPath: String,
    ): TruvideoSdkVideoEncodeBuilder = TruvideoSdkVideoEncodeBuilder(
        engine = encodeEngine,
        repository = videoRequestRepository,
        videoPath = videoPath,
        resultPath = resultPath
    )

    override val environment: String
        get() = BuildConfig.FLAVOR
}