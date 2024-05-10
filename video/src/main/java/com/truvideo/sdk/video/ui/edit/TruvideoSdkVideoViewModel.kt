package com.truvideo.sdk.video.ui.edit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truvideo.sdk.video.TruvideoSdkVideo
import com.truvideo.sdk.video.model.EditMode
import com.truvideo.sdk.video.model.TruvideoSdkVideoInformation
import com.truvideo.sdk.video.model.TruvideoSdkVideoRotation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

internal class TruvideoSdkVideoViewModel(
    val videoPath: String,
    private val outputPath: String,
) : ViewModel() {

    private val _start: MutableStateFlow<Long> = MutableStateFlow(0L)
    private val _end: MutableStateFlow<Long> = MutableStateFlow(0L)
    private val _videoInfo: MutableStateFlow<TruvideoSdkVideoInformation?> = MutableStateFlow(null)
    private val _volume: MutableStateFlow<Float> = MutableStateFlow(1f)
    private val _returnToApplication: MutableStateFlow<String?> = MutableStateFlow(null)
    private val _returnToAppError: MutableStateFlow<String?> = MutableStateFlow(null)
    private val _isInitializing: MutableStateFlow<Boolean> = MutableStateFlow(true)
    private val _isProcessing: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _editMode: MutableStateFlow<EditMode> = MutableStateFlow(EditMode.Trimming)
    private val _videoRotation: MutableStateFlow<Float> = MutableStateFlow(0f)
    val previewItemCount: Int = 10
    private val _videoFrames: MutableStateFlow<List<TimeLineFrame>> = MutableStateFlow(emptyList())

    val start: StateFlow<Long> =
        _start.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)
    val end: StateFlow<Long> =
        _end.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val isInitializing =
        _isInitializing.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val isProcessing =
        _isProcessing.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val resultPath =
        _returnToApplication.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val errorResult =
        _returnToAppError.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val videoInfo = _videoInfo.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val volume = _volume.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 1f)

    val editMode =
        _editMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EditMode.Trimming)

    val videoRotation =
        _videoRotation.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0f)

    val videoFrames =
        _videoFrames.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), listOf())

    fun updateStart(value: Long) {
        _start.update { value }
    }

    fun updateEnd(value: Long) {
        _end.update { value }
    }

    fun updateVolume(value: Float) {
        _volume.update { value }
    }

    fun updateEditionMode(mode: EditMode) {
        _editMode.update { mode }
    }

    fun updateRotation(rotation: Float) {
        _videoRotation.update { _videoRotation.value + rotation }
    }

    fun init(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            initialize(context)
        }
    }

    private suspend fun initialize(context: Context) {
        _isInitializing.value = true
        val info = TruvideoSdkVideo.getInfo(videoPath)
        _start.value = 0L
        _end.value = info.durationMillis.toLong()
        _videoInfo.value = info
        _isInitializing.value = false

        fetchPreviews(context)
    }

    private suspend fun fetchPreviews(context: Context) {
        val result = mutableListOf<TimeLineFrame>()
        for (i in 0 until previewItemCount) {
            result.add(TimeLineFrame(i, null, true))
        }
        _videoFrames.value = listOf()
        val videoInfo = TruvideoSdkVideo.getInfo(videoPath)
        val videoLengthInMs = videoInfo.durationMillis

        val path = "${context.filesDir.path}/thumb_${System.currentTimeMillis()}.png"
        val interval = videoLengthInMs / previewItemCount
        for (i in 0 until previewItemCount) {
            TruvideoSdkVideo.createThumbnail(videoPath, path, (i * interval).toLong(), 100)
            val bitmap = BitmapFactory.decodeFile(path)
            File(path).delete()

            val isActive = viewModelScope.isActive
            if (isActive) {
                val currentList = videoFrames.value;
                _videoFrames.value = currentList + TimeLineFrame(i, bitmap, false)
                delay(50)
                _videoFrames.value = currentList + TimeLineFrame(i, bitmap, true)
            }
        }
    }

    fun trimVideo() {
        _isProcessing.update { true }
        _returnToAppError.update { null }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val start = _start.value
                val end = _end.value
                val rotation = videoRotation.value
                val volume = volume.value

                val finalRotation = rotation + (videoInfo.value?.rotation ?: 0)

                TruvideoSdkVideo.edit(
                    videoPath,
                    outputPath,
                    start,
                    end,
                    volume,
                    when (finalRotation) {
                        90F -> TruvideoSdkVideoRotation.DEGREES_270
                        180F -> TruvideoSdkVideoRotation.DEGREES_180
                        270F -> TruvideoSdkVideoRotation.DEGREES_90
                        else -> TruvideoSdkVideoRotation.DEGREES_ZERO
                    },
                )
                _returnToApplication.update { outputPath }
            } catch (e: Exception) {
                e.printStackTrace()
                _isProcessing.update { false }
                _returnToAppError.update { "$e" }
            }
        }
    }


}

class TimeLineFrame(val index: Int, val bitmap: Bitmap?, val visible: Boolean)