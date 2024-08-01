package com.truvideo.sdk.video.video_request_builder

import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoBuilder
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoCallback
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoRequestEngine
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoRequestRepository
import com.truvideo.sdk.video.model.TruvideoSdkVideoConcatRequestData
import com.truvideo.sdk.video.model.TruvideoSdkVideoException
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequest
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TruvideoSdkVideoConcatBuilder(
    val videoPaths: List<String>,
    val resultPath: String,
) : TruvideoSdkVideoBuilder {

    private val scope = CoroutineScope(Dispatchers.IO)
    internal lateinit var engine: TruvideoSdkVideoRequestEngine
    internal lateinit var repository: TruvideoSdkVideoRequestRepository

    override suspend fun build(): TruvideoSdkVideoRequest {
        val request = TruvideoSdkVideoRequest.build(TruvideoSdkVideoRequestType.CONCAT, engine)
        request.concatData = TruvideoSdkVideoConcatRequestData(
            videoPaths = videoPaths,
            resultPath = resultPath,
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