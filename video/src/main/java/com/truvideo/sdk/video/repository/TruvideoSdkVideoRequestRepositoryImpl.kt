package com.truvideo.sdk.video.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.truvideo.sdk.video.data.DatabaseInstance
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoLogAdapter
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoRequestRepository
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequest
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestStatus
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import truvideo.sdk.common.exception.TruvideoSdkException
import truvideo.sdk.common.model.TruvideoSdkLogSeverity
import java.util.Date

internal class TruvideoSdkVideoRequestRepositoryImpl(
    private val context: Context,
    private val logAdapter: TruvideoSdkVideoLogAdapter
) : TruvideoSdkVideoRequestRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    override suspend fun insert(videoRequest: TruvideoSdkVideoRequest): TruvideoSdkVideoRequest {
        logAdapter.addLog(
            eventName = "event_video_request_insert",
            message = "Insert video request: ${videoRequest.toJson()}",
            severity = TruvideoSdkLogSeverity.INFO,
        )

        DatabaseInstance.getDatabase(context).videoRequestDao().insert(videoRequest)
        return videoRequest
    }

    override suspend fun update(videoRequest: TruvideoSdkVideoRequest): TruvideoSdkVideoRequest {
        logAdapter.addLog(
            eventName = "event_video_request_update",
            message = "Update video request: ${videoRequest.toJson()}",
            severity = TruvideoSdkLogSeverity.INFO,
        )

        videoRequest.updatedAtMillis = Date().time
        DatabaseInstance.getDatabase(context).videoRequestDao().update(videoRequest)
        return videoRequest
    }

    override suspend fun getById(id: String): TruvideoSdkVideoRequest? {
        logAdapter.addLog(
            eventName = "event_video_request_get_by_id",
            message = "Get request by id: $id",
            severity = TruvideoSdkLogSeverity.INFO,
        )

        return DatabaseInstance.getDatabase(context).videoRequestDao().getById(id)
    }

    override suspend fun getAll(status: TruvideoSdkVideoRequestStatus?): List<TruvideoSdkVideoRequest> {
        logAdapter.addLog(
            eventName = "event_video_request_get_all",
            message = "Get all video request with status: ${status?.name}",
            severity = TruvideoSdkLogSeverity.INFO
        )
        return if (status != null) {
            DatabaseInstance.getDatabase(context).videoRequestDao().getAllByStatus(status)
        } else {
            DatabaseInstance.getDatabase(context).videoRequestDao().getAll()
        }
    }

    override fun streamById(id: String): LiveData<TruvideoSdkVideoRequest?> {
        logAdapter.addLog(
            eventName = "event_video_request_stream_by_id",
            message = "Stream video request by id: $id",
            severity = TruvideoSdkLogSeverity.INFO
        )

        return DatabaseInstance.getDatabase(context).videoRequestDao().streamById(id)
    }

    override fun streamAll(
        status: TruvideoSdkVideoRequestStatus?
    ): LiveData<List<TruvideoSdkVideoRequest>> {
        logAdapter.addLog(
            eventName = "event_video_request_stream_all",
            message = "Stream all video request with status: ${status?.name}",
            severity = TruvideoSdkLogSeverity.INFO
        )

        return if (status != null) {
            DatabaseInstance.getDatabase(context).videoRequestDao().streamAllByStatus(status)
        } else {
            DatabaseInstance.getDatabase(context).videoRequestDao().streamAll()
        }
    }

    override suspend fun delete(id: String) {
        logAdapter.addLog(
            "event_video_request_delete",
            "Delete video request by id: $id",
            TruvideoSdkLogSeverity.INFO
        )

        val request = getById(id)
        if (request != null) {
            DatabaseInstance.getDatabase(context).videoRequestDao().deleteById(request)
        }

    }

    override suspend fun tryChangeStatusToProcessing(id: String) {
        try {
            val request = getById(id) ?: throw TruvideoSdkException("Request not found")
            request.status = TruvideoSdkVideoRequestStatus.PROCESSING
            request.errorMessage = null
            request.commandId = null
            request.progress = 0f
            update(request)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    override suspend fun tryChangeStatusToCompleted(id: String, resultPath: String) {
        try {
            val request = getById(id) ?: throw TruvideoSdkException("Request not found")
            when (request.type) {
                TruvideoSdkVideoRequestType.MERGE -> request.mergeData = request.mergeData?.copy(resultPath = resultPath)
                TruvideoSdkVideoRequestType.CONCAT -> request.concatData = request.concatData?.copy(resultPath = resultPath)
                TruvideoSdkVideoRequestType.ENCODE -> request.encodeData = request.encodeData?.copy(resultPath = resultPath)
            }
            request.status = TruvideoSdkVideoRequestStatus.COMPLETED
            request.errorMessage = null
            request.commandId = null
            request.progress = 1f
            update(request)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    override suspend fun tryChangeStatusToCanceled(id: String) {
        try {
            val request = getById(id) ?: throw TruvideoSdkException("Request not found")
            request.status = TruvideoSdkVideoRequestStatus.CANCELED
            request.errorMessage = null
            request.commandId = null
            request.progress = 0f
            update(request)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    override suspend fun tryChangeStatusToError(id: String, errorMessage: String) {
        try {
            val request = getById(id) ?: throw TruvideoSdkException("Request not found")
            request.status = TruvideoSdkVideoRequestStatus.ERROR
            request.errorMessage = errorMessage
            request.commandId = null
            request.progress = 0f
            update(request)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    override suspend fun tryUpdateCommandId(id: String, commandId: Long) {
        try {
            val request = getById(id) ?: throw TruvideoSdkException("Request not found")
            request.commandId = commandId
            update(request)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    override suspend fun tryUpdateProgress(id: String, progress: Float) {
        try {
            val request = getById(id) ?: throw TruvideoSdkException("Request not found")
            request.progress = progress
            update(request)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    override suspend fun cancelAllProcessing() {
        val items = mutableListOf<TruvideoSdkVideoRequest>()
        items.addAll(getAll(TruvideoSdkVideoRequestStatus.PROCESSING))

        items.forEach {
            it.status = TruvideoSdkVideoRequestStatus.CANCELED
            it.commandId = null
            it.errorMessage = null
            update(it)
        }
    }
}