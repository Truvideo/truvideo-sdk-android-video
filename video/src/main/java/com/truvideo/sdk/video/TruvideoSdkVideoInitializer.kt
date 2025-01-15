package com.truvideo.sdk.video

import android.content.Context
import androidx.startup.Initializer
import com.truvideo.sdk.video.adapters.TruvideoSdkVideoAuthAdapterImpl
import com.truvideo.sdk.video.adapters.TruvideoSdkVideoFFmpegAdapterImpl
import com.truvideo.sdk.video.adapters.TruvideoSdkVideoLogAdapterImpl
import com.truvideo.sdk.video.adapters.TruvideoSdkVideoVersionPropertiesAdapterImpl
import com.truvideo.sdk.video.engines.TruvideoSdkConcatVideoRequestEngineImpl
import com.truvideo.sdk.video.engines.TruvideoSdkEncodeVideoRequestEngineImpl
import com.truvideo.sdk.video.engines.TruvideoSdkMergeVideoRequestEngineImpl
import com.truvideo.sdk.video.repository.TruvideoSdkVideoRequestRepositoryImpl
import com.truvideo.sdk.video.usecases.ClearNoiseUseCase
import com.truvideo.sdk.video.usecases.CompareVideosUseCase
import com.truvideo.sdk.video.usecases.ConcatVideosUseCase
import com.truvideo.sdk.video.usecases.CreateVideoThumbnailUseCase
import com.truvideo.sdk.video.usecases.EditVideoUseCase
import com.truvideo.sdk.video.usecases.ExtractAudioUseCase
import com.truvideo.sdk.video.usecases.GetVideoInfoUseCase
import com.truvideo.sdk.video.usecases.MergeVideosUseCase
import com.truvideo.sdk.video.usecases.ReplaceAudioTrackUseCase

@Suppress("unused")
class TruvideoSdkVideoInitializer : Initializer<Unit> {

    companion object {
        fun init(context: Context) {
            val ffmpegAdapter = TruvideoSdkVideoFFmpegAdapterImpl()

            val versionPropertiesAdapter = TruvideoSdkVideoVersionPropertiesAdapterImpl(
                context = context
            )

            val logAdapter = TruvideoSdkVideoLogAdapterImpl(
                versionPropertiesAdapter = versionPropertiesAdapter
            )

            val authAdapter = TruvideoSdkVideoAuthAdapterImpl(
                logAdapter = logAdapter,
                versionPropertiesAdapter = versionPropertiesAdapter
            )

            val getVideoInfoUseCase = GetVideoInfoUseCase(
                context = context,
                ffmpegAdapter = ffmpegAdapter
            )

            val compareVideosUseCase = CompareVideosUseCase(
                getVideoInfoUseCase = getVideoInfoUseCase
            )

            val extractAudioUseCase = ExtractAudioUseCase(
                context = context,
                ffmpegAdapter = ffmpegAdapter
            )

            val replaceAudioTrackUseCase = ReplaceAudioTrackUseCase(
                context = context,
                ffmpegAdapter = ffmpegAdapter
            )

            val clearVideoAudioNoiseUseCase = ClearNoiseUseCase(
                context = context,
                extractAudioUseCase = extractAudioUseCase,
                replaceAudioTrackUseCase = replaceAudioTrackUseCase,
            )

            val createVideoThumbnailUseCase = CreateVideoThumbnailUseCase(
                context = context,
                ffmpegAdapter = ffmpegAdapter
            )
            val editVideoUseCase = EditVideoUseCase(
                context = context,
                getVideoInfoUseCase = getVideoInfoUseCase,
                ffmpegAdapter = ffmpegAdapter
            )

            val concatVideosUseCase = ConcatVideosUseCase(
                context = context,
                ffmpegAdapter = ffmpegAdapter,
            )

            val mergeVideosUseCase = MergeVideosUseCase(
                context = context,
                ffmpegAdapter = ffmpegAdapter,
                getVideoInfoUseCase = getVideoInfoUseCase
            )

            val videoRequestRepository = TruvideoSdkVideoRequestRepositoryImpl(
                context = context,
                logAdapter = logAdapter
            )

            val mergeEngine = TruvideoSdkMergeVideoRequestEngineImpl(
                authAdapter = authAdapter,
                ffmpegAdapter = ffmpegAdapter,
                videoRequestRepository = videoRequestRepository,
                mergeVideosUseCase = mergeVideosUseCase,
                getVideoInfoUseCase = getVideoInfoUseCase
            )

            val encodeEngine = TruvideoSdkEncodeVideoRequestEngineImpl(
                authAdapter = authAdapter,
                ffmpegAdapter = ffmpegAdapter,
                videoRequestRepository = videoRequestRepository,
                getVideoInfoUseCase = getVideoInfoUseCase,
                mergeVideosUseCase = mergeVideosUseCase
            )

            val concatEngine = TruvideoSdkConcatVideoRequestEngineImpl(
                authAdapter = authAdapter,
                ffmpegAdapter = ffmpegAdapter,
                videoRequestRepository = videoRequestRepository,
                concatVideosUseCase = concatVideosUseCase,
                getVideoInfoUseCase = getVideoInfoUseCase,
                compareVideosUseCase = compareVideosUseCase
            )

            TruvideoSdkVideo = TruvideoSdkVideoImpl(
                context = context,
                authAdapter = authAdapter,
                getVideoInfoUseCase = getVideoInfoUseCase,
                compareVideosUseCase = compareVideosUseCase,
                mergeEngine = mergeEngine,
                encodeEngine = encodeEngine,
                concatEngine = concatEngine,
                videoRequestRepository = videoRequestRepository,
                clearVideoAudioNoiseUseCase = clearVideoAudioNoiseUseCase,
                createVideoThumbnailUseCase = createVideoThumbnailUseCase,
                editVideoUseCase = editVideoUseCase,
                logAdapter = logAdapter,
                versionPropertiesAdapter = versionPropertiesAdapter
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