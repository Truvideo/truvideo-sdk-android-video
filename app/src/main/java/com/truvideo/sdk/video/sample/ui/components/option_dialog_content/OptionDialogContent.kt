package com.truvideo.sdk.video.sample.ui.components.option_dialog_content

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.truvideo.sdk.video.sample.ui.theme.TruVideoSdkVideoModuleTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf


@Composable
fun OptionDialogContent(
    title: String = "",
    options: ImmutableList<OptionDialogModel> = persistentListOf()
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AlertDialogDefaults.containerColor)
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column {
            if (title.trim().isNotEmpty()) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            } else {
                Box(modifier = Modifier.height(16.dp))
            }

            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                options.forEach {
                    ListTile(
                        text = it.title,
                        icon = it.icon,
                        onPressed = it.onPressed
                    )
                }
            }

            Box(modifier = Modifier.height(16.dp))
        }
    }
}


@Composable
private fun ListTile(
    text: String = "",
    icon: ImageVector? = null,
    onPressed: () -> Unit = {}
) {
    Row(modifier = Modifier
        .clickable { onPressed() }
        .padding(16.dp)
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = "")
            Box(modifier = Modifier.width(16.dp))
        }

        Text(text, modifier = Modifier.weight(1f))
    }
}


data class OptionDialogModel(
    val title: String = "",
    val icon: ImageVector? = null,
    val onPressed: () -> Unit = {}
)

@Composable
@Preview(showBackground = true)
private fun Test() {
    TruVideoSdkVideoModuleTheme {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            OptionDialogContent(
                title = "Options",
                options = persistentListOf(
                    OptionDialogModel(title = "option 1"),
                    OptionDialogModel(title = "option 2"),
                    OptionDialogModel(title = "option 3"),
                    OptionDialogModel(title = "option 1"),
                    OptionDialogModel(title = "option 2"),
                    OptionDialogModel(title = "option 3"),
                    OptionDialogModel(title = "option 1"),
                    OptionDialogModel(title = "option 2"),
                    OptionDialogModel(title = "option 3"),
                    OptionDialogModel(title = "option 1"),
                    OptionDialogModel(title = "option 2"),
                    OptionDialogModel(title = "option 3"),
                    OptionDialogModel(title = "option 1"),
                    OptionDialogModel(title = "option 2"),
                    OptionDialogModel(title = "option 3"),
                    OptionDialogModel(title = "option 1"),
                    OptionDialogModel(title = "option 2"),
                    OptionDialogModel(title = "option 3"),
                    OptionDialogModel(title = "option 1"),
                    OptionDialogModel(title = "option 2"),
                    OptionDialogModel(title = "option 3"),

                    )
            )
        }
    }
}