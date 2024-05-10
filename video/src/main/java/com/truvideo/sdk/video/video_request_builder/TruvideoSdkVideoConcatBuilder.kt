package com.truvideo.sdk.video.video_request_builder

import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoBuilder
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoBuilderCallback
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoRequestEngine
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoRequestRepository
import com.truvideo.sdk.video.model.TruvideoSdkVideoConcatRequestData
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequest
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import truvideo.sdk.common.exception.TruvideoSdkException

class TruvideoSdkVideoConcatBuilder(
    val videoPaths: List<String>,
    val resultPath: String,
    private val engine: TruvideoSdkVideoRequestEngine,
    private val repository: TruvideoSdkVideoRequestRepository
) : TruvideoSdkVideoBuilder {

    private val scope = CoroutineScope(Dispatchers.IO)

    override suspend fun build(): TruvideoSdkVideoRequest {
        val request = TruvideoSdkVideoRequest.build(TruvideoSdkVideoRequestType.CONCAT, engine)
        request.concatData = TruvideoSdkVideoConcatRequestData(
            videoPaths = videoPaths,
            resultPath = resultPath,
        )
        repository.insert(request)
        return request
    }

    override fun build(callback: TruvideoSdkVideoBuilderCallback) {
        scope.launch {
            try {
                val request = build()
                callback.onReady(request)
            } catch (exception: Exception) {
                exception.printStackTrace()
                val truvideoException = if (exception is TruvideoSdkException) exception else TruvideoSdkException(
                    exception.localizedMessage ?: "Unknown error"
                )
                callback.onError(truvideoException)
            }
        }
    }

}