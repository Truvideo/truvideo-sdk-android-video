package com.truvideo.sdk.video.model

import kotlinx.serialization.Serializable

@Serializable
data class TruvideoSdkVideoMergeMediaEntry(
    val fileIndex: Int,
    val entryIndex: Long
) {
    companion object {
        fun empty(fileIndex: Int): TruvideoSdkVideoMergeMediaEntry {
            return TruvideoSdkVideoMergeMediaEntry(
                fileIndex = fileIndex,
                entryIndex = -1
            )
        }
    }
}
