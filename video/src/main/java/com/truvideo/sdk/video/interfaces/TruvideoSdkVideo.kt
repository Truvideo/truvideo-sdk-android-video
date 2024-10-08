/**
 * Interface for interacting with Truvideo SDK video functionality.
 */
package com.truvideo.sdk.video.interfaces

import androidx.lifecycle.LiveData
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile
import com.truvideo.sdk.video.model.TruvideoSdkVideoFileDescriptor
import com.truvideo.sdk.video.model.TruvideoSdkVideoInformation
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequest
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestStatus
import com.truvideo.sdk.video.model.TruvideoSdkVideoRotation
import com.truvideo.sdk.video.video_request_builder.TruvideoSdkVideoConcatBuilder
import com.truvideo.sdk.video.video_request_builder.TruvideoSdkVideoEncodeBuilder
import com.truvideo.sdk.video.video_request_builder.TruvideoSdkVideoMergeBuilder

/**
 * Defines methods for various video-related operations using the Truvideo SDK,
 * such as clearing noise, creating thumbnails, editing, merging, concatenating,
 * checking video readiness, and retrieving information about videos.
 */
@Suppress("FunctionName")
interface TruvideoSdkVideo {

    suspend fun getAllRequests(status: TruvideoSdkVideoRequestStatus? = null): List<TruvideoSdkVideoRequest>

    /**
     * Retrieves a LiveData object containing a list of video requests filtered by the specified status.
     * The LiveData object will be updated whenever there are changes in the underlying data.
     *
     * @param status The status of the video requests to retrieve.
     * @return A LiveData object containing a list of {@link TruvideoSdkVideoVideoRequest} objects matching the specified status.
     */
    fun streamAllRequests(status: TruvideoSdkVideoRequestStatus? = null): LiveData<List<TruvideoSdkVideoRequest>>


    /**
     * Clears noise from a video
     * and outputs the result into another video
     *
     * @param input The input video.
     * @param output The output video file with noise cleared.
     *
     * authenticated with Truvideo SDK.
     */
    suspend fun clearNoise(
        input: TruvideoSdkVideoFile,
        output: TruvideoSdkVideoFileDescriptor,
    ): String

    /**
     * Clears noise from a video
     * and outputs the result into another file
     *
     * @param input The input video file.
     * @param output The output video with noise cleared.
     * @param callback The callback to be invoked upon completion or failure of the operation.
     */
    fun clearNoise(
        input: TruvideoSdkVideoFile,
        output: TruvideoSdkVideoFileDescriptor,
        callback: TruvideoSdkVideoCallback<String>
    )

    /**
     * Asynchronously checks if the specified list of video are compatible to be concat.
     *
     * @param input The list of videos to be checked.
     * @return {@code true} if the videos are compatible to be concatenated, {@code false} otherwise.
     */
    suspend fun compare(input: List<TruvideoSdkVideoFile>): Boolean

    /**
     * Checks if the specified list of video are compatible to be concat.
     *
     * @param input The list of videos to be checked.
     * @param callback The callback to notify upon successful completion or in case of an error.
     */
    fun compare(
        input: List<TruvideoSdkVideoFile>,
        callback: TruvideoSdkVideoCallback<Boolean>
    )

    /**
     * Asynchronously retrieves information about a video.
     *
     * @param input The video for which information is requested.
     * @return A {@link VideoInfoModel} containing information about the video
     */
    suspend fun getInfo(input: TruvideoSdkVideoFile): TruvideoSdkVideoInformation

    /**
     * Asynchronously retrieves information about a video and notifies a callback upon completion.
     *
     * @param input The video for which information is requested.
     * @param callback The callback to notify upon successful completion or in case of an error.
     */
    fun getInfo(
        input: TruvideoSdkVideoFile,
        callback: TruvideoSdkVideoCallback<TruvideoSdkVideoInformation>
    )


    /**
     * Creates a thumbnail for a video at the specified position.
     *
     * @param input The video.
     * @param position The position in the video to capture the thumbnail.
     * @param width The width of the thumbnail.
     * @param height The height of the thumbnail.
     */
    suspend fun createThumbnail(
        input: TruvideoSdkVideoFile,
        output: TruvideoSdkVideoFileDescriptor,
        position: Long = 1000,
        width: Int? = null,
        height: Int? = null,
        precise: Boolean = false
    ): String

    /**
     * Creates a thumbnail for a video at the specified position.
     *
     * @param input The path of the video file.
     * @param position The position in the video to capture the thumbnail.
     * @param width The width of the thumbnail.
     * @param height The height of the thumbnail.
     * @param callback The callback to handle thumbnail creation completion.
     */
    fun createThumbnail(
        input: TruvideoSdkVideoFile,
        output: TruvideoSdkVideoFileDescriptor,
        position: Long = 1000,
        width: Int? = null,
        height: Int? = null,
        precise: Boolean = false,
        callback: TruvideoSdkVideoCallback<String>
    )

    suspend fun edit(
        input: TruvideoSdkVideoFile,
        output: TruvideoSdkVideoFileDescriptor,
        start: Long? = null,
        end: Long? = null,
        volume: Float = 1.0f,
        rotation: TruvideoSdkVideoRotation? = null,
    ): String

    fun edit(
        input: TruvideoSdkVideoFile,
        output: TruvideoSdkVideoFileDescriptor,
        start: Long? = null,
        end: Long? = null,
        volume: Float = 1.0f,
        rotation: TruvideoSdkVideoRotation? = null,
        callback: TruvideoSdkVideoCallback<String>
    )

//    suspend fun removeAudio(
//        inputPath: String,
//        outputPath: String
//    )
//
//    suspend fun removeVideo(
//        inputPath: String,
//        outputPath: String
//    )
//
//    suspend fun addVideoTrack(
//        inputPath: String,
//        outputPath: String
//    )
//
//    suspend fun addAudioTrack(
//        inputPath: String,
//        outputPath: String
//    )

    fun MergeBuilder(
        input: List<TruvideoSdkVideoFile>,
        output: TruvideoSdkVideoFileDescriptor
    ): TruvideoSdkVideoMergeBuilder

    fun ConcatBuilder(
        input: List<TruvideoSdkVideoFile>,
        output: TruvideoSdkVideoFileDescriptor
    ): TruvideoSdkVideoConcatBuilder

    fun EncodeBuilder(
        input: TruvideoSdkVideoFile,
        output: TruvideoSdkVideoFileDescriptor
    ): TruvideoSdkVideoEncodeBuilder

    val environment: String
}