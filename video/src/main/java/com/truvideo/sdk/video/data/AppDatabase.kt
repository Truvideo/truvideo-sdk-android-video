package com.truvideo.sdk.video.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequest

@Database(entities = [TruvideoSdkVideoRequest::class], version = 1)
@TypeConverters(DatabaseConverters::class)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun videoRequestDao(): VideoRequestDao
}
