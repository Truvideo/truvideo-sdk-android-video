package com.truvideo.sdk.video.data

import androidx.room.TypeConverter
import com.truvideo.sdk.video.model.TruvideoSdkVideoConcatRequestData
import com.truvideo.sdk.video.model.TruvideoSdkVideoEncodeRequestData
import com.truvideo.sdk.video.model.TruvideoSdkVideoMergeRequestData
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestStatus
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestType
import java.util.Date

internal class DatabaseConverters {

    @TypeConverter
    fun fromEncodeRequestData(data: TruvideoSdkVideoEncodeRequestData?): String? {
        return data?.toJson()
    }

    @TypeConverter
    fun toEncodeRequestData(json: String?): TruvideoSdkVideoEncodeRequestData? {
        return json?.let { TruvideoSdkVideoEncodeRequestData.fromJson(it) }
    }

    @TypeConverter
    fun fromConcatRequestData(data: TruvideoSdkVideoConcatRequestData?): String? {
        return data?.toJson()
    }

    @TypeConverter
    fun toConcatRequestData(json: String?): TruvideoSdkVideoConcatRequestData? {
        return json?.let { TruvideoSdkVideoConcatRequestData.fromJson(it) }
    }

    @TypeConverter
    fun fromMergeRequestData(data: TruvideoSdkVideoMergeRequestData?): String? {
        return data?.toJson()
    }

    @TypeConverter
    fun toMergeRequestData(json: String?): TruvideoSdkVideoMergeRequestData? {
        return json?.let { TruvideoSdkVideoMergeRequestData.fromJson(it) }
    }

    @TypeConverter
    fun fromStatus(status: TruvideoSdkVideoRequestStatus): String {
        return status.name
    }

    @TypeConverter
    fun toStatus(status: String): TruvideoSdkVideoRequestStatus {
        return enumValueOf<TruvideoSdkVideoRequestStatus>(status)
    }

    @TypeConverter
    fun fromType(status: TruvideoSdkVideoRequestType): String {
        return status.name
    }

    @TypeConverter
    fun toType(status: String): TruvideoSdkVideoRequestType {
        return enumValueOf<TruvideoSdkVideoRequestType>(status)
    }

    @TypeConverter
    fun fromDate(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun toDate(date: Date?): Long? {
        return date?.time
    }

}
