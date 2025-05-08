package com.truvideo.sdk.video.video_request_builder

import android.content.Context
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoBuilder
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoCallback
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoRequestEngine
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoRequestRepository
import com.truvideo.sdk.video.model.TruvideoSdkVideoEncodeRequestData
import com.truvideo.sdk.video.model.TruvideoSdkVideoEncodeVideoEntry
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile
import com.truvideo.sdk.video.model.TruvideoSdkVideoFileDescriptor
import com.truvideo.sdk.video.model.TruvideoSdkVideoFrameRate
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequest
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import truvideo.sdk.common.exceptions.TruvideoSdkException

class TruvideoSdkVideoEncodeBuilder(
    val input: TruvideoSdkVideoFile,
    val output: TruvideoSdkVideoFileDescriptor,

) : TruvideoSdkVideoBuilder {

    private val scope = CoroutineScope(Dispatchers.IO)
    internal lateinit var context: Context
    internal lateinit var engine: TruvideoSdkVideoRequestEngine
    internal lateinit var repository: TruvideoSdkVideoRequestRepository

    var width: Int? = null
    var height: Int? = null

    @Suppress("MemberVisibilityCanBePrivate")
    var framesRate: TruvideoSdkVideoFrameRate = TruvideoSdkVideoFrameRate.defaultFrameRate

    @Suppress("MemberVisibilityCanBePrivate")
    var videoTracks: List<TruvideoSdkVideoEncodeVideoEntry> = listOf()

    @Suppress("MemberVisibilityCanBePrivate")
    var audioTracks: List<Long> = listOf()

    override suspend fun build(): TruvideoSdkVideoRequest {
        try {
            val request = TruvideoSdkVideoRequest.build(TruvideoSdkVideoRequestType.ENCODE, engine)
            val inputPath = input.getPath(context)
            request.encodeData = TruvideoSdkVideoEncodeRequestData(
                inputPath = inputPath,
                outputPath = output.getDescription(context),
                resultPath = "",
                width = width,
                height = height,
                framesRate = framesRate,
                videoTracks = videoTracks,
                audioTracks = audioTracks
            )
            repository.insert(request)
            return request
        } catch (exception: Exception) {
            exception.printStackTrace()

            if (exception is TruvideoSdkException) {
                throw exception
            } else {
                throw TruvideoSdkException("Unknown error")
            }
        }
    }

    override fun build(callback: TruvideoSdkVideoCallback<TruvideoSdkVideoRequest>) {
        scope.launch {
            try {
                val request = build()
                callback.onComplete(request)
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