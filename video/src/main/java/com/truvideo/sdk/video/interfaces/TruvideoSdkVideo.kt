/**
 * Interface for interacting with Truvideo SDK video functionality.
 */
package com.truvideo.sdk.video.interfaces

import androidx.activity.ComponentActivity
import androidx.lifecycle.LiveData
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
     * Initializes the Truvideo SDK within the specified activity.
     *
     * @param activity The activity where the SDK should be initialized.
     */
    fun initEditScreen(activity: ComponentActivity)

    /**
     * Edits a video specified by its URI using the Truvideo SDK.
     *
     * @param videoPath The path of the original video.
     * @param callback The function to handle the edited video result.
     * @param resultPath The destination path that the output .
     */
    fun openEditScreen(
        videoPath: String,
        resultPath: String,
        callback: TruvideoSdkVideoEditCallback
    )

    /**
     * Clears noise from a video file (originVideoFilePath)
     * and outputs the result into another video file (destinationVideoFilePath).
     *
     * @param videoPath The original video file path.
     * @param resultPath The output video file with noise cleared.
     *
     * authenticated with Truvideo SDK.
     */
    suspend fun clearNoise(
        videoPath: String,
        resultPath: String
    )

    /**
     * Clears noise from a video file (originVideoFilePath)
     * and outputs the result into another video file (destinationVideoFilePath).
     *
     * @param videoPath The original video file path.
     * @param resultPath The output video file with noise cleared.
     * @param callback The callback to be invoked upon completion or failure of the operation.
     */
    fun clearNoise(
        videoPath: String,
        resultPath: String,
        callback: TruvideoSdkVideoClearNoiseCallback
    )

    /**
     * Asynchronously checks if the specified list of video URIs are compatible to be concat.
     *
     * @param videoPaths The list of video path to be checked.
     * @return {@code true} if the videos are ready to be concatenated, {@code false} otherwise.
     */
    suspend fun compare(videoPaths: List<String>): Boolean

    /**
     * Checks if the specified list of video URIs are compatible to be concat.
     *
     * @param videoPaths The list of video path to be checked.
     * @param callback The callback to notify upon successful completion or in case of an error.
     */
    fun compare(videoPaths: List<String>, callback: TruvideoSdkVideoAreVideosReadyToConcatCallback)

    /**
     * Asynchronously retrieves information about a video specified by its URI.
     *
     * @param videoPath The path of the video for which information is requested.
     * @return A {@link VideoInfoModel} containing information about the video
     */
    suspend fun getInfo(videoPath: String): TruvideoSdkVideoInformation

    /**
     * Asynchronously retrieves information about a video specified by its URI and notifies a callback upon completion.
     *
     * @param videoPath The path of the video for which information is requested.
     * @param callback The callback to notify upon successful completion or in case of an error.
     */
    fun getInfo(videoPath: String, callback: TruvideoSdkVideoGetVideoInfoCallback)

    /**
     * Creates a thumbnail for a video file at the specified position.
     *
     * @param videoPath The path of the video file.
     * @param resultPath The path for the generated thumbnail.
     * @param position The position in the video to capture the thumbnail.
     * @param width The width of the thumbnail.
     * @param height The height of the thumbnail.
     * @param callback The callback to handle thumbnail creation completion.
     */
    fun createThumbnail(
        videoPath: String,
        resultPath: String,
        position: Long = 1000,
        width: Int? = null,
        height: Int? = null,
        callback: TruvideoSdkVideoThumbnailCallback
    )

    /**
     * Creates a thumbnail for a video file at the specified position.
     *
     * @param videoPath The path of the video file.
     * @param resultPath The path for the generated thumbnail.
     * @param position The position in the video to capture the thumbnail.
     * @param width The width of the thumbnail.
     * @param height The height of the thumbnail.
     */
    suspend fun createThumbnail(
        videoPath: String,
        resultPath: String,
        position: Long = 1000,
        width: Int? = null,
        height: Int? = null
    )


    suspend fun edit(
        videoPath: String,
        resultPath: String,
        start: Long? = null,
        end: Long? = null,
        volume: Float = 1.0f,
        rotation: TruvideoSdkVideoRotation? = null,
    )

    fun MergeBuilder(
        videoPaths: List<String>,
        resultPath: String
    ): TruvideoSdkVideoMergeBuilder

    fun ConcatBuilder(
        videoPaths: List<String>,
        resultPath: String
    ): TruvideoSdkVideoConcatBuilder

    fun EncodeBuilder(
        videoPath: String,
        resultPath: String
    ): TruvideoSdkVideoEncodeBuilder

    val environment: String
}