package com.truvideo.sdk.video.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.truvideo.sdk.video.data.DatabaseConverters
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoCallback
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoRequestEngine
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Date
import java.util.UUID

@Serializable
@Entity
data class TruvideoSdkVideoRequest(
    @PrimaryKey val id: String,
    var errorMessage: String? = null,
    var commandId: Long? = null,
    val createdAtMillis: Long,
    var updatedAtMillis: Long,
    var progress: Float = 0f,
    @TypeConverters(DatabaseConverters::class) var status: TruvideoSdkVideoRequestStatus,
    @TypeConverters(DatabaseConverters::class) var type: TruvideoSdkVideoRequestType,
    @TypeConverters(DatabaseConverters::class) var mergeData: TruvideoSdkVideoMergeRequestData? = null,
    @TypeConverters(DatabaseConverters::class) var concatData: TruvideoSdkVideoConcatRequestData? = null,
    @TypeConverters(DatabaseConverters::class) var encodeData: TruvideoSdkVideoEncodeRequestData? = null
) {
    fun toJson(): String = Json.encodeToString(
        mapOf(
            "id" to id,
            "errorMessage" to (errorMessage ?: ""),
            "commandId" to (commandId?.toString() ?: ""),
            "status" to status.name,
            "type" to type.name,
            "createdAt" to createdAt.time.toString(),
            "updatedAt" to updatedAt.time.toString()
        )
    )


    @Ignore
    private lateinit var engine: TruvideoSdkVideoRequestEngine

    suspend fun process() = engine.process(id)

    fun process(callback: TruvideoSdkVideoCallback<String>) = engine.process(id, callback)

    suspend fun cancel() = engine.cancel(id)

    fun cancel(callback: TruvideoSdkVideoCallback<Unit>) = engine.cancel(id, callback)

    suspend fun delete() = engine.delete(id)

    fun delete(callback: TruvideoSdkVideoCallback<Unit>) = engine.delete(id, callback)

    internal fun setEngine(engine: TruvideoSdkVideoRequestEngine) {
        this.engine = engine
    }

    val createdAt: Date
        get() {
            return Date(createdAtMillis)
        }

    val updatedAt: Date
        get() {
            return Date(updatedAtMillis)
        }

    companion object {
        internal fun build(
            type: TruvideoSdkVideoRequestType,
            engine: TruvideoSdkVideoRequestEngine
        ): TruvideoSdkVideoRequest {
            val videoRequest = TruvideoSdkVideoRequest(
                id = UUID.randomUUID().toString(),
                createdAtMillis = System.currentTimeMillis(),
                updatedAtMillis = System.currentTimeMillis(),
                status = TruvideoSdkVideoRequestStatus.IDLE,
                type = type
            )

            videoRequest.engine = engine
            return videoRequest
        }
    }

}


