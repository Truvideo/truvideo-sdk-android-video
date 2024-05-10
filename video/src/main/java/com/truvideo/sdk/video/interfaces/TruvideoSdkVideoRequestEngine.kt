/**
 * Interface for interacting with Truvideo SDK video functionality.
 */
package com.truvideo.sdk.video.interfaces

import truvideo.sdk.common.exception.TruvideoSdkException

/**
 * Defines methods for clearing noise, creating thumbnails, initializing,
 * editing, merging, concatenating, checking video readiness, and retrieving
 * information about videos using the Truvideo SDK.
 */
interface TruvideoSdkVideoRequestEngine {

    /**
     * Asynchronously process a video request.
     *
     * @param id The video request id to be processed.
     * @throws TruvideoSdkException If an error occurs during the process operation, including SDK-specific errors.
     */
    suspend fun process(id: String)

    /**
     * Asynchronously process a video request and notifies a callback upon completion.
     *
     * @param id The video request id to be processed.
     * @param callback The callback to notify upon successful completion or in case of an error.
     */
    fun process(
        id: String,
        callback: TruvideoSdkVideoJoinCallback
    )

    /**
     * Asynchronously cancels a video processing request.
     *
     * @param id The video request id to be canceled.
     */
    suspend fun cancel(id: String)

    /**
     * Asynchronously cancels a video processing request.
     * This method cancels the ongoing video processing operation initiated by the provided context
     * and notifies the specified callback upon completion or in case of an error.
     *
     * @param id The video request id to be canceled.
     * @param callback The callback to notify upon successful cancellation or in case of an error.
     */
    fun cancel(
        id: String,
        callback: TruvideoSdkVideoCancelCallback
    )

    suspend fun delete(id: String)
}