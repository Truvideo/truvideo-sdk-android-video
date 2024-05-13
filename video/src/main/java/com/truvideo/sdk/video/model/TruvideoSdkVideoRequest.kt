package com.truvideo.sdk.video.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.truvideo.sdk.video.data.DatabaseConverters
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoCancelCallback
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoJoinCallback
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoRequestEngine
import java.util.Date
import java.util.UUID

@Entity
data class TruvideoSdkVideoRequest(
    @PrimaryKey val id: String,
    var errorMessage: String? = null,
    var commandId: Long? = null,
    @TypeConverters(DatabaseConverters::class) var status: TruvideoSdkVideoRequestStatus,
    @TypeConverters(DatabaseConverters::class) var type: TruvideoSdkVideoRequestType,
    @TypeConverters(DatabaseConverters::class) val createdAt: Date = Date(),
    @TypeConverters(DatabaseConverters::class) var updatedAt: Date = Date(),
    @TypeConverters(DatabaseConverters::class) var mergeData: TruvideoSdkVideoMergeRequestData? = null,
    @TypeConverters(DatabaseConverters::class) var concatData: TruvideoSdkVideoConcatRequestData? = null,
    @TypeConverters(DatabaseConverters::class) var encodeData: TruvideoSdkVideoEncodeRequestData? = null
) {
    @Ignore
    private lateinit var engine: TruvideoSdkVideoRequestEngine

    suspend fun process() = engine.process(id)

    fun process(callback: TruvideoSdkVideoJoinCallback) = engine.process(id, callback)

    suspend fun cancel() = engine.cancel(id)

    suspend fun delete() = engine.delete(id)

    fun cancel(callback: TruvideoSdkVideoCancelCallback) = engine.cancel(id, callback)
    fun setEngine(engine: TruvideoSdkVideoRequestEngine) {
        this.engine = engine
    }

    companion object {
        internal fun build(
            type: TruvideoSdkVideoRequestType, engine: TruvideoSdkVideoRequestEngine
        ): TruvideoSdkVideoRequest {
            val videoRequest = TruvideoSdkVideoRequest(
                id = UUID.randomUUID().toString(),
                status = TruvideoSdkVideoRequestStatus.IDLE,
                type = type
            )

            videoRequest.engine = engine
            return videoRequest
        }
    }

}


