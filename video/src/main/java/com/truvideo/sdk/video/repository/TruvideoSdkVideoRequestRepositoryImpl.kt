package com.truvideo.sdk.video.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.truvideo.sdk.video.data.DatabaseInstance
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoLogAdapter
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoRequestEngine
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoRequestRepository
import com.truvideo.sdk.video.model.TruvideoSdkVideoException
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequest
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestStatus
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
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

        return suspendCancellableCoroutine { cont ->
            scope.launch {
                DatabaseInstance.getDatabase(context).videoRequestDao().insert(videoRequest)
                cont.resumeWith(Result.success(videoRequest))
            }
        }
    }

    override suspend fun update(videoRequest: TruvideoSdkVideoRequest): TruvideoSdkVideoRequest {
        logAdapter.addLog(
            eventName = "event_video_request_update",
            message = "Update video request: ${videoRequest.toJson()}",
            severity = TruvideoSdkLogSeverity.INFO,
        )

        return suspendCancellableCoroutine { cont ->
            scope.launch {
                videoRequest.updatedAt = Date()
                DatabaseInstance.getDatabase(context).videoRequestDao().update(videoRequest)
                cont.resumeWith(Result.success(videoRequest))
            }
        }
    }

    override suspend fun getById(id: String): TruvideoSdkVideoRequest? {
        logAdapter.addLog(
            eventName = "event_video_request_get_by_id",
            message = "Get request by id: $id",
            severity = TruvideoSdkLogSeverity.INFO,
        )

        return suspendCancellableCoroutine { cont ->
            scope.launch {
                val request = DatabaseInstance.getDatabase(context).videoRequestDao().getById(id)
                cont.resumeWith(Result.success(request))
            }
        }
    }

    override suspend fun getAll(status: TruvideoSdkVideoRequestStatus?): List<TruvideoSdkVideoRequest> {
        logAdapter.addLog(
            eventName = "event_video_request_get_all",
            message = "Get all video request with status: ${status?.name}",
            severity = TruvideoSdkLogSeverity.INFO
        )

        return suspendCancellableCoroutine { cont ->
            scope.launch {
                val data = if (status != null) {
                    DatabaseInstance.getDatabase(context).videoRequestDao().getAllByStatus(status)
                } else {
                    DatabaseInstance.getDatabase(context).videoRequestDao().getAll()
                }
                cont.resumeWith(Result.success(data))
            }
        }
    }

    override suspend fun streamById(id: String): LiveData<TruvideoSdkVideoRequest?> {
        logAdapter.addLog(
            eventName = "event_video_request_stream_by_id",
            message = "Stream video request by id: $id",
            severity = TruvideoSdkLogSeverity.INFO
        )

        return suspendCancellableCoroutine { cont ->
            scope.launch {
                val data = DatabaseInstance.getDatabase(context).videoRequestDao().streamById(id)
                cont.resumeWith(Result.success(data))
            }
        }
    }

    override fun streamAll(
        status: TruvideoSdkVideoRequestStatus?,
        mergeEngine: TruvideoSdkVideoRequestEngine,
        concatEngine: TruvideoSdkVideoRequestEngine,
        encodeEngine: TruvideoSdkVideoRequestEngine
    ): LiveData<List<TruvideoSdkVideoRequest>> {
        logAdapter.addLog(
            eventName = "event_video_request_stream_all",
            message = "Stream all video request with status: ${status?.name}",
            severity = TruvideoSdkLogSeverity.INFO
        )

        val data = if (status != null) {
            DatabaseInstance.getDatabase(context).videoRequestDao().streamAllByStatus(status)
        } else {
            DatabaseInstance.getDatabase(context).videoRequestDao().streamAll()
        }

        val mappedData = MediatorLiveData<List<TruvideoSdkVideoRequest>>()

        mappedData.addSource(data) { list ->
            val mappedList = list.map { request ->
                when (request.type) {
                    TruvideoSdkVideoRequestType.MERGE -> request.setEngine(mergeEngine)
                    TruvideoSdkVideoRequestType.CONCAT -> request.setEngine(concatEngine)
                    TruvideoSdkVideoRequestType.ENCODE -> request.setEngine(encodeEngine)
                }
                return@map request
            }
            mappedData.value = mappedList
        }

        return mappedData
    }

    override suspend fun delete(id: String) {
        logAdapter.addLog(
            "event_video_request_delete",
            "Delete video request by id: $id",
            TruvideoSdkLogSeverity.INFO
        )

        suspendCancellableCoroutine { cont ->
            scope.launch {
                val request = getById(id)
                if (request != null) {
                    DatabaseInstance.getDatabase(context).videoRequestDao().deleteById(request)
                }
                cont.resumeWith(Result.success(Unit))
            }
        }
    }

    override suspend fun tryChangeStatusToProcessing(id: String) {
        try {
            val request = getById(id) ?: throw TruvideoSdkVideoException("Request not found")
            request.status = TruvideoSdkVideoRequestStatus.PROCESSING
            request.errorMessage = null
            request.commandId = null
            update(request)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    override suspend fun tryChangeStatusToCompleted(id: String) {
        try {
            val request = getById(id) ?: throw TruvideoSdkVideoException("Request not found")
            request.status = TruvideoSdkVideoRequestStatus.COMPLETED
            request.errorMessage = null
            request.commandId = null
            update(request)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    override suspend fun tryChangeStatusToCanceled(id: String) {
        try {
            val request = getById(id) ?: throw TruvideoSdkVideoException("Request not found")
            request.status = TruvideoSdkVideoRequestStatus.CANCELED
            request.errorMessage = null
            request.commandId = null
            update(request)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    override suspend fun tryChangeStatusToError(id: String, errorMessage: String) {
        try {
            val request = getById(id) ?: throw TruvideoSdkVideoException("Request not found")
            request.status = TruvideoSdkVideoRequestStatus.ERROR
            request.errorMessage = errorMessage
            request.commandId = null
            update(request)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    override suspend fun tryUpdateCommandId(id: String, commandId: Long) {
        try {
            val request = getById(id) ?: throw TruvideoSdkVideoException("Request not found")
            request.commandId = commandId
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