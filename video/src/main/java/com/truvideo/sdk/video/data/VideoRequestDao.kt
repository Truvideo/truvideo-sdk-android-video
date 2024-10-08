package com.truvideo.sdk.video.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequest
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestStatus

@Dao
interface VideoRequestDao {

    @Delete
    suspend fun deleteById(videoRequest: TruvideoSdkVideoRequest)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(videoRequest: TruvideoSdkVideoRequest)

    @Update
    suspend fun update(videoRequest: TruvideoSdkVideoRequest)

    @Query("SELECT * FROM TruvideoSdkVideoRequest WHERE id = :id")
    suspend fun getById(id: String): TruvideoSdkVideoRequest?

    @Query("SELECT * FROM TruvideoSdkVideoRequest")
    suspend fun getAll(): List<TruvideoSdkVideoRequest>

    @Query("SELECT * FROM TruvideoSdkVideoRequest WHERE status = :status")
    suspend fun getAllByStatus(status: TruvideoSdkVideoRequestStatus): List<TruvideoSdkVideoRequest>

    @Query("SELECT * FROM TruvideoSdkVideoRequest WHERE id = :id")
    fun streamById(id: String): LiveData<TruvideoSdkVideoRequest?>

    @Query("SELECT * FROM TruvideoSdkVideoRequest")
    fun streamAll(): LiveData<List<TruvideoSdkVideoRequest>>

    @Query("SELECT * FROM TruvideoSdkVideoRequest WHERE status = :status")
    fun streamAllByStatus(status: TruvideoSdkVideoRequestStatus): LiveData<List<TruvideoSdkVideoRequest>>
}
