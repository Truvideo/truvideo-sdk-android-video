package com.truvideo.sdk.video.ui.activities.edit.utils

import java.util.Formatter

internal object TruvideoSdkVideoUtils {

    fun timeToScreen(timeMs: Long): String {
        val totalMilliseconds = timeMs.toInt()
        val totalSeconds = totalMilliseconds / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        val mFormatter = Formatter()

        return if (hours != 0) {
            mFormatter.format("%02d:%02d:%02d", hours, minutes, seconds).toString()
        } else {
            mFormatter.format("%02d:%02d", minutes, seconds).toString()
        }
    }
}
