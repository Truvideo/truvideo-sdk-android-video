package com.truvideo.sdk.video

import androidx.activity.ComponentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideo
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoAreVideosReadyToConcatCallback
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoAuthAdapter
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoClearNoiseCallback
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoEditCallback
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoGetVideoInfoCallback
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoRequestEngine
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoRequestRepository
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoThumbnailCallback
import com.truvideo.sdk.video.model.SdkFeature
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
import truvideo.sdk.common.exception.TruvideoSdkException
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

    override fun initEditScreen(activity: ComponentActivity) {
        openEditScreenUseCase.init(activity)
    }

    override fun clearNoise(
        videoPath: String, resultPath: String, callback: TruvideoSdkVideoClearNoiseCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                clearNoise(
                    videoPath,
                    resultPath,
                )
                callback.onReady(resultPath)
            } catch (exception: Exception) {
                if (exception is TruvideoSdkException) {
                    callback.onError(exception)
                } else {
                    callback.onError(TruvideoSdkException(exception.message ?: ""))
                }
            }
        }
    }

    override suspend fun clearNoise(
        videoPath: String, resultPath: String
    ) {
        authAdapter.validateAuthentication()

        try {
            authAdapter.verifyFeatureAvailable(SdkFeature.NOISE_CANCELLING)
            clearVideoAudioNoiseUseCase(
                videoPath, resultPath
            )
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

    override fun openEditScreen(
        videoPath: String, resultPath: String, callback: TruvideoSdkVideoEditCallback
    ) {
        authAdapter.validateAuthentication()

        try {
            openEditScreenUseCase.edit(
                videoPath,
                resultPath,
                callback
            )
        } catch (ex: Exception) {
            ex.printStackTrace()

            if (ex is TruvideoSdkException) {
                throw ex
            } else {
                throw TruvideoSdkException(ex.message ?: "")
            }
        }
    }

    override suspend fun compare(videoPaths: List<String>): Boolean {
        authAdapter.validateAuthentication()

        try {
            return compareVideosUseCase(videoPaths)
        } catch (ex: Exception) {
            ex.printStackTrace()

            if (ex is TruvideoSdkException) {
                throw ex
            } else {
                throw TruvideoSdkException(ex.message ?: "")
            }
        }
    }

    override fun compare(
        videoPaths: List<String>, callback: TruvideoSdkVideoAreVideosReadyToConcatCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val areReady = compare(videoPaths)
                callback.onReady(areReady)
            } catch (exception: Exception) {
                if (exception is TruvideoSdkException) {
                    callback.onError(exception)
                } else {
                    callback.onError(TruvideoSdkException(exception.message ?: ""))
                }
            }
        }
    }

    override suspend fun getInfo(videoPath: String): TruvideoSdkVideoInformation {
        authAdapter.validateAuthentication()

        try {
            return getVideoInfoUseCase(videoPath)
        } catch (ex: Exception) {
            ex.printStackTrace()

            if (ex is TruvideoSdkException) {
                throw ex
            } else {
                throw TruvideoSdkException(ex.message ?: "")
            }
        }
    }

    override fun getInfo(
        videoPath: String, callback: TruvideoSdkVideoGetVideoInfoCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val truvideoSdkVideoInfo = getInfo(videoPath)
                callback.onReady(truvideoSdkVideoInfo)
            } catch (exception: Exception) {
                if (exception is TruvideoSdkException) {
                    callback.onError(exception)
                } else {
                    callback.onError(TruvideoSdkException(exception.message ?: ""))
                }
            }
        }
    }

    override fun createThumbnail(
        videoPath: String,
        resultPath: String,
        position: Long,
        width: Int?,
        height: Int?,
        callback: TruvideoSdkVideoThumbnailCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                createThumbnail(
                    videoPath, resultPath, position, width, height
                )
                callback.onReady(resultPath)
            } catch (exception: Exception) {
                if (exception is TruvideoSdkException) {
                    callback.onError(exception)
                } else {
                    callback.onError(TruvideoSdkException(exception.message ?: ""))
                }
            }
        }
    }

    override suspend fun createThumbnail(
        videoPath: String, resultPath: String, position: Long, width: Int?, height: Int?
    ) {
        authAdapter.validateAuthentication()

        try {
            createVideoThumbnailUseCase(
                videoPath,
                resultPath,
                position.toDuration(DurationUnit.MILLISECONDS),
                width,
                height,
            )
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

    override suspend fun edit(
        videoPath: String,
        resultPath: String,
        start: Long?,
        end: Long?,
        volume: Float,
        rotation: TruvideoSdkVideoRotation?
    ) {
        authAdapter.validateAuthentication()

        try {
            editVideoUseCase(
                videoPath,
                resultPath,
                start,
                end,
                rotation,
                volume
            )

        } catch (ex: Exception) {
            ex.printStackTrace()

            if (ex is TruvideoSdkException) {
                throw ex
            } else {
                throw TruvideoSdkException(ex.message ?: "")
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