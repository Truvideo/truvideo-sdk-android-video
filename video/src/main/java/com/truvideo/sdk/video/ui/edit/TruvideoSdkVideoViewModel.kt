package com.truvideo.sdk.video.ui.edit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

internal class TruvideoSdkVideoViewModel(
    val inputFilePath: String,
    private val outputFilePath: String
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
        _start.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0L
        )
    val end: StateFlow<Long> = _end.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0L
    )

    val isInitializing = _isInitializing.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = true
    )

    val isProcessing = _isProcessing.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
    )

    val resultPath = _returnToApplication.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    val errorResult = _returnToAppError.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    val videoInfo = _videoInfo.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    val volume = _volume.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 1f
    )

    val editMode = _editMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EditMode.Trimming
    )

    val videoRotation = _videoRotation.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0f
    )

    val videoFrames = _videoFrames.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = listOf()
    )

    fun updateStart(value: Long) {
        _start.value = value
    }

    fun updateEnd(value: Long) {
        _end.value = value
    }

    fun updateVolume(value: Float) {
        _volume.value = value
    }

    fun updateEditionMode(value: EditMode) {
        _editMode.value = value
    }

    fun updateRotation(value: Float) {
        _videoRotation.value += value
    }

    fun init(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            initialize(context)
        }
    }

    private suspend fun initialize(context: Context) {
        _isInitializing.value = true
        val info = TruvideoSdkVideo.getInfo(inputFilePath)
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
        val videoInfo = TruvideoSdkVideo.getInfo(inputFilePath)
        val videoLengthInMs = videoInfo.durationMillis

        val interval = videoLengthInMs / previewItemCount
        val path = "${context.filesDir.path}/thumb_${System.currentTimeMillis()}.png"

        for (i in 0 until previewItemCount) {
            try {
                val thumbnailPath = TruvideoSdkVideo.createThumbnail(
                    videoPath = inputFilePath,
                    resultPath = path,
                    position = (i * interval).toLong(),
                    width = 100
                )

                val bitmap = BitmapFactory.decodeFile(thumbnailPath)
                File(thumbnailPath).delete()
                val isActive = viewModelScope.isActive
                if (isActive) {
                    val currentList = videoFrames.value
                    _videoFrames.value = currentList + TimeLineFrame(i, bitmap, false)
                    delay(50)
                    _videoFrames.value = currentList + TimeLineFrame(i, bitmap, true)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun apply() {
        _isProcessing.value = true
        _returnToAppError.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val start = _start.value
                val end = _end.value
                val rotation = videoRotation.value
                val volume = volume.value

                val originalRotation = (videoInfo.value?.rotation ?: 0)
                var finalRotation = rotation + originalRotation
                finalRotation %= 360
                if (finalRotation < 0) finalRotation += 360

                val outputFile = File(outputFilePath)
                if (outputFile.exists()) outputFile.delete()

                TruvideoSdkVideo.edit(
                    videoPath = inputFilePath,
                    resultPath = outputFilePath,
                    start = start,
                    end = end,
                    volume = volume,
                    rotation = when (finalRotation) {
                        0f -> TruvideoSdkVideoRotation.DEGREES_ZERO
                        90F -> TruvideoSdkVideoRotation.DEGREES_90
                        180F -> TruvideoSdkVideoRotation.DEGREES_180
                        270F -> TruvideoSdkVideoRotation.DEGREES_270
                        else -> null
                    },
                )

                _returnToApplication.value = outputFilePath
            } catch (exception: Exception) {
                exception.printStackTrace()
                _isProcessing.value = false
                _returnToAppError.value = exception.localizedMessage
            }
        }
    }
}

internal data class TimeLineFrame(val index: Int, val bitmap: Bitmap?, val visible: Boolean)