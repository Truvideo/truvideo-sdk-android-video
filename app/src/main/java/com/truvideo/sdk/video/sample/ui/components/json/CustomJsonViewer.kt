package com.truvideo.sdk.video.sample.ui.components.json

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.truvideo.sdk.video.model.TruvideoSdkVideoAudioTrackInformation
import com.truvideo.sdk.video.model.TruvideoSdkVideoInformation
import com.truvideo.sdk.video.model.TruvideoSdkVideoTrackInformation
import com.truvideo.sdk.video.sample.ui.theme.TruVideoSdkVideoModuleTheme
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun CustomJsonViewer(data: String = "") {
    val json = remember(data) { Json.parseToJsonElement(data) }
    JsonElementPreview(jsonElement = json)
}

@Composable
private fun JsonElementPreview(
    jsonElement: JsonElement,
    depth: Int = 0
) {
    when (jsonElement) {
        is JsonObject -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(0.1f))
                    .padding(8.dp)

            ) {
                JsonObjectPreview(
                    jsonObject = jsonElement.jsonObject,
                    depth = depth
                )
            }
        }

        is JsonArray -> {
            if (jsonElement.isEmpty()) {
                Text(
                    "Empty",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    JsonArrayPreview(
                        jsonArray = jsonElement.jsonArray,
                        depth = depth
                    )
                }
            }
        }

        is JsonPrimitive -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    jsonElement.content,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun JsonArrayPreview(
    jsonArray: JsonArray,
    depth: Int = 0
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        jsonArray.forEach { childObject ->
            JsonElementPreview(
                jsonElement = childObject,
                depth = depth + 1
            )
        }
    }
}

@Composable
private fun JsonObjectPreview(
    jsonObject: JsonObject,
    depth: Int = 0
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        jsonObject.entries.forEach {

            when (it.value) {
                is JsonPrimitive -> {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            it.key,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier
                                .wrapContentWidth()
                        )
                        Text(
                            it.value.jsonPrimitive.content,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                

                is JsonArray -> {
                    if (it.value.jsonArray.isEmpty()) {
                        Row {
                            Text(
                                it.key,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.weight(1f)
                            )
                            Text("Empty list", style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        Column {
                            Text(it.key, style = MaterialTheme.typography.titleSmall)
                            JsonElementPreview(
                                jsonElement = it.value,
                                depth = depth + 1
                            )
                        }
                    }
                }

                else -> {
                    Column {
                        Text(it.key, style = MaterialTheme.typography.titleSmall)
                        JsonElementPreview(
                            jsonElement = it.value,
                            depth = depth + 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun Test() {
    TruVideoSdkVideoModuleTheme {
        CustomJsonViewer(
            data = TruvideoSdkVideoInformation(
                path = "kojasdo qsjdikasjd kasjdkl asjdklasj dlasjdkl ajlkdjaskldjaskldjaskl djaslkdjaskl djaksld jalkdj lkadj lkasdalsk djlaksd",
                durationMillis = 0,
                size = 0,
                audioTracks = listOf(TruvideoSdkVideoAudioTrackInformation.empty()),
                videoTracks = listOf(TruvideoSdkVideoTrackInformation.empty()),
                format = ""
            ).toJson()
        )
    }
}