/**
 * Interface for interacting with Truvideo SDK video functionality.
 */
package com.truvideo.sdk.video.interfaces

import com.truvideo.sdk.video.model.TruvideoSdkVideoRequest

/**
 * Defines methods for clearing noise, creating thumbnails, initializing,
 * editing, merging, concatenating, checking video readiness, and retrieving
 * information about videos using the Truvideo SDK.
 */
internal interface TruvideoSdkVideoRequestEngine {

    suspend fun process(id: String): String

    fun process(
        id: String,
        callback: TruvideoSdkVideoCallback<String>
    )

    suspend fun cancel(id: String)

    fun cancel(
        id: String,
        callback: TruvideoSdkVideoCallback<Unit>
    )

    suspend fun delete(id: String)

    fun delete(
        id: String,
        callback: TruvideoSdkVideoCallback<Unit>
    )

    suspend fun update(request: TruvideoSdkVideoRequest)

    fun update(
        request: TruvideoSdkVideoRequest,
        callback: TruvideoSdkVideoCallback<Unit>
    )
}