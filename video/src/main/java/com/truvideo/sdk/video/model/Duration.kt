package com.truvideo.sdk.video.model

import kotlin.time.Duration

internal fun Duration.ffmpegFormat(): String =
    this.toComponents { hours, minutes, seconds, _ -> "${String.format("%02d",hours)}:${String.format("%02d",minutes)}:${String.format("%02d",seconds)}" }