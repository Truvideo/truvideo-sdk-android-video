package com.truvideo.sdk.video.video_request_builder

import android.content.Context
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoBuilder
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoCallback
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoRequestEngine
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoRequestRepository
import com.truvideo.sdk.video.model.TruvideoSdkVideoConcatRequestData
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile
import com.truvideo.sdk.video.model.TruvideoSdkVideoFileDescriptor
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequest
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import truvideo.sdk.common.exceptions.TruvideoSdkException

class TruvideoSdkVideoConcatBuilder(
    val input: List<TruvideoSdkVideoFile>,
    val output: TruvideoSdkVideoFileDescriptor,
) : TruvideoSdkVideoBuilder {

    private val scope = CoroutineScope(Dispatchers.IO)
    internal lateinit var context: Context
    internal lateinit var engine: TruvideoSdkVideoRequestEngine
    internal lateinit var repository: TruvideoSdkVideoRequestRepository

    override suspend fun build(): TruvideoSdkVideoRequest {
        try {
            val request = TruvideoSdkVideoRequest.build(TruvideoSdkVideoRequestType.CONCAT, engine)
            request.concatData = TruvideoSdkVideoConcatRequestData(
                inputPaths = input.map { it.getPath(context) },
                outputPath = output.getDescription(context),
                resultPath = ""
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