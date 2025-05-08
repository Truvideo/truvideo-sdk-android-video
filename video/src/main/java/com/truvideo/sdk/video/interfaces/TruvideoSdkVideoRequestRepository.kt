package com.truvideo.sdk.video.interfaces

import androidx.lifecycle.LiveData
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequest
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestStatus

internal interface TruvideoSdkVideoRequestRepository {

    suspend fun insert(videoRequest: TruvideoSdkVideoRequest): TruvideoSdkVideoRequest

    suspend fun update(videoRequest: TruvideoSdkVideoRequest): TruvideoSdkVideoRequest

    suspend fun getById(id: String): TruvideoSdkVideoRequest?

    suspend fun getAll(status: TruvideoSdkVideoRequestStatus? = null): List<TruvideoSdkVideoRequest>

    fun streamById(id: String): LiveData<TruvideoSdkVideoRequest?>

    fun streamAll(status: TruvideoSdkVideoRequestStatus? = null): LiveData<List<TruvideoSdkVideoRequest>>

    suspend fun delete(id: String)

    suspend fun tryChangeStatusToProcessing(id: String)

    suspend fun tryChangeStatusToCompleted(id: String, resultPath: String)

    suspend fun tryChangeStatusToCanceled(id: String)

    suspend fun tryChangeStatusToError(id: String, errorMessage: String)

    suspend fun tryUpdateCommandId(id: String, commandId: Long)

    suspend fun tryUpdateProgress(id: String, progress: Float)

    suspend fun cancelAllProcessing()
}