package com.truvideo.sdk.video.sample.ui.dialogs


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.truvideo.sdk.video.TruvideoSdkVideo
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile
import com.truvideo.sdk.video.model.TruvideoSdkVideoFileDescriptor
import com.truvideo.sdk.video.sample.ui.theme.TruVideoSdkVideoModuleTheme
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThumbnailDialog(
    visible: Boolean = true,
    close: () -> Unit = {},
    path: String
) {
    if (visible) {
        BasicAlertDialog(
            onDismissRequest = { close() }
        ) {
            Content(
                path = path
            )
        }
    }
}

@Composable
private fun Content(
    path: String
) {
    var height by remember { mutableStateOf("") }
    var width by remember { mutableStateOf("") }
    var position by remember { mutableStateOf("") }
    var thumbnail by remember { mutableStateOf("") }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    val scope = rememberCoroutineScope()

    fun createThumbnail(height: Int, width: Int, position: Long, path: String) {
        scope.launch {
            val file = TruvideoSdkVideoFile.custom(path)
            thumbnail = TruvideoSdkVideo.createThumbnail(
                input = file,
                output = TruvideoSdkVideoFileDescriptor.cache("thumbnail_${System.currentTimeMillis()}"),
                height = height,
                width = width,
                position = position,
                precise = true
            )
            val fileThumbnail = File(thumbnail)
            bitmap = if (fileThumbnail.exists()) BitmapFactory.decodeFile(fileThumbnail.absolutePath) else null
        }
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AlertDialogDefaults.containerColor)
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = height,
                modifier = Modifier.fillMaxWidth(),
                onValueChange = { if (it.all { char -> char.isDigit() }) height = it },
                label = { Text("Height") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = width,
                modifier = Modifier.fillMaxWidth(),
                onValueChange = { if (it.all { char -> char.isDigit() }) width = it },
                label = { Text("Width") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = position,
                modifier = Modifier.fillMaxWidth(),
                onValueChange = { if (it.all { char -> char.isDigit() }) position = it },
                label = { Text("Position") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )

            Image(
                bitmap =(bitmap?.asImageBitmap()
                    ?: ImageBitmap.imageResource(id = android.R.drawable.ic_menu_gallery)),
                contentDescription = "",
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Button(
                onClick = {
                    val h = height.toIntOrNull() ?: 0
                    val w = width.toIntOrNull() ?: 0
                    val p = position.toLongOrNull() ?: 0
                    createThumbnail(h,w,p,path)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create")
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun Test() {
    TruVideoSdkVideoModuleTheme {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Content(path = "")
        }
    }
}