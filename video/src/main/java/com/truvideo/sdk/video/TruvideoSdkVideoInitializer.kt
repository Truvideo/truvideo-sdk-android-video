package com.truvideo.sdk.video

import android.content.Context
import androidx.startup.Initializer
import com.truvideo.sdk.video.adapters.AuthAdapterImpl
import com.truvideo.sdk.video.adapters.FFmpegAdapterImpl
import com.truvideo.sdk.video.adapters.VersionPropertiesAdapter
import com.truvideo.sdk.video.engines.TruvideoSdkConcatVideoRequestEngineImpl
import com.truvideo.sdk.video.engines.TruvideoSdkEncodeVideoRequestEngineImpl
import com.truvideo.sdk.video.engines.TruvideoSdkMergeVideoRequestEngineImpl
import com.truvideo.sdk.video.managers.TruvideoSdkVideoMediaManagerImpl
import com.truvideo.sdk.video.repository.TruvideoSdkVideoRequestRepositoryImpl
import com.truvideo.sdk.video.usecases.ClearVideoAudioNoiseUseCase
import com.truvideo.sdk.video.usecases.CompareVideosUseCase
import com.truvideo.sdk.video.usecases.CreateVideoThumbnailUseCase
import com.truvideo.sdk.video.usecases.EditVideoUseCase
import com.truvideo.sdk.video.usecases.GenerateConcatVideosCommandUseCase
import com.truvideo.sdk.video.usecases.GenerateEncodeVideosCommandUseCase
import com.truvideo.sdk.video.usecases.GenerateMergeVideosCommandUseCase
import com.truvideo.sdk.video.usecases.GetVideoInfoUseCase
import com.truvideo.sdk.video.usecases.GetVideoSizeUseCase
import com.truvideo.sdk.video.usecases.OpenEditScreenUseCase

@Suppress("unused")
class TruvideoSdkVideoInitializer : Initializer<Unit> {

    companion object {
        fun init(context: Context) {
            val ffmpegAdapter = FFmpegAdapterImpl()
            val versionPropertiesAdapter = VersionPropertiesAdapter(context)
            val authAdapter = AuthAdapterImpl(
                versionPropertiesAdapter = versionPropertiesAdapter
            )
            val getVideoSizeUseCase = GetVideoSizeUseCase(
                context = context
            )
            val getVideoInfoUseCase = GetVideoInfoUseCase(
                ffmpegAdapter = ffmpegAdapter, getVideoSizeUseCase = getVideoSizeUseCase
            )
            val compareVideosUseCase = CompareVideosUseCase(
                getVideoInfoUseCase = getVideoInfoUseCase
            )
            val mediaManager = TruvideoSdkVideoMediaManagerImpl(
                ffmpegAdapter = ffmpegAdapter
            )

            val clearVideoAudioNoiseUseCase = ClearVideoAudioNoiseUseCase(
                mediaManager = mediaManager,
                context = context
            )
            val createVideoThumbnailUseCase = CreateVideoThumbnailUseCase(
                ffmpegAdapter = ffmpegAdapter
            )

            val editVideoUseCase = EditVideoUseCase(
                getVideoInfoUseCase = getVideoInfoUseCase,
                ffmpegAdapter = ffmpegAdapter
            )

            val openEditScreenUseCase = OpenEditScreenUseCase()

            val generateMergeVideosCommandUseCase = GenerateMergeVideosCommandUseCase(
                getVideoInfoUseCase = getVideoInfoUseCase
            )
            val generateConcatVideosCommandUseCase = GenerateConcatVideosCommandUseCase()
            val generateEncodeVideosCommandUseCase = GenerateEncodeVideosCommandUseCase(
                getVideoInfoUseCase = getVideoInfoUseCase
            )

            val videoRequestRepository = TruvideoSdkVideoRequestRepositoryImpl(
                context = context,
            )

            val mergeEngine = TruvideoSdkMergeVideoRequestEngineImpl(
                authAdapter = authAdapter,
                ffmpegAdapter = ffmpegAdapter,
                videoRequestRepository = videoRequestRepository,
                generateMergeVideosCommandUseCase = generateMergeVideosCommandUseCase
            )

            val concatEngine = TruvideoSdkConcatVideoRequestEngineImpl(
                context = context,
                authAdapter = authAdapter,
                ffmpegAdapter = ffmpegAdapter,
                videoRequestRepository = videoRequestRepository,
                compareVideosUseCase = compareVideosUseCase,
                generateConcatVideosCommandUseCase = generateConcatVideosCommandUseCase
            )

            val encodeEngine = TruvideoSdkEncodeVideoRequestEngineImpl(
                authAdapter = authAdapter,
                ffmpegAdapter = ffmpegAdapter,
                videoRequestRepository = videoRequestRepository,
                generateEncodeVideosCommandUseCase = generateEncodeVideosCommandUseCase,
            )

            TruvideoSdkVideo = TruvideoSdkVideoImpl(
                authAdapter = authAdapter,
                getVideoInfoUseCase = getVideoInfoUseCase,
                compareVideosUseCase = compareVideosUseCase,
                concatEngine = concatEngine,
                mergeEngine = mergeEngine,
                encodeEngine = encodeEngine,
                videoRequestRepository = videoRequestRepository,
                clearVideoAudioNoiseUseCase = clearVideoAudioNoiseUseCase,
                createVideoThumbnailUseCase = createVideoThumbnailUseCase,
                openEditScreenUseCase = openEditScreenUseCase,
                editVideoUseCase = editVideoUseCase,
            )
        }
    }

    override fun create(context: Context) {
        init(context)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }
}