package com.truvideo.sdk.video.usecases.model

internal data class VideoTrack(
    val width: Int,
    val height: Int,
    val tracks: List<VideoItem>
)