package com.truvideo.sdk.video.video_request_builder

import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoBuilder
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoCallback
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoRequestEngine
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoRequestRepository
import com.truvideo.sdk.video.model.TruvideoSdkVideoEncodeRequestData
import com.truvideo.sdk.video.model.TruvideoSdkVideoException
import com.truvideo.sdk.video.model.TruvideoSdkVideoFrameRate
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequest
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestType
import com.truvideo.sdk.video.model.TruvideoSdkVideoVideoCodec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TruvideoSdkVideoEncodeBuilder(
    val videoPath: String,
    val resultPath: String
) : TruvideoSdkVideoBuilder {

    private val scope = CoroutineScope(Dispatchers.IO)
    internal lateinit var engine: TruvideoSdkVideoRequestEngine
    internal lateinit var repository: TruvideoSdkVideoRequestRepository

    var width: Int? = null
    var height: Int? = null
    var videoCodec: TruvideoSdkVideoVideoCodec = TruvideoSdkVideoVideoCodec.defaultCodec
    var framesRate: TruvideoSdkVideoFrameRate = TruvideoSdkVideoFrameRate.defaultFrameRate

    override suspend fun build(): TruvideoSdkVideoRequest {
        val request = TruvideoSdkVideoRequest.build(TruvideoSdkVideoRequestType.ENCODE, engine)
        request.encodeData = TruvideoSdkVideoEncodeRequestData(
            videoPath = videoPath,
            resultPath = resultPath,
            receivedWidth = width,
            receivedHeight = height,
            videoCodec = videoCodec,
            framesRate = framesRate
        )
        repository.insert(request)
        return request
    }

    override fun build(callback: TruvideoSdkVideoCallback<TruvideoSdkVideoRequest>) {
        scope.launch {
            try {
                val request = build()
                callback.onComplete(request)
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
}