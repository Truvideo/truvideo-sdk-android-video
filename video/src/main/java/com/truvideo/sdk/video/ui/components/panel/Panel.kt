package com.truvideo.sdk.video.ui.components.panel

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.truvideo.sdk.components.animated_fade_visibility.TruvideoAnimatedFadeVisibility
import com.truvideo.sdk.components.button.TruvideoIconButton
import com.truvideo.sdk.video.ui.activities.edit.theme.TruvideoSdkTheme

@Composable
fun Panel(
    visible: Boolean = true,
    close: (() -> Unit) = {},
    closeVisible: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable BoxScope.() -> Unit = {}
) {
    BackHandler(visible) {
        close()
    }

    TruvideoAnimatedFadeVisibility(visible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(0.9f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() })
                {
                    close()
                }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (closeVisible) {
                        TruvideoIconButton(
                            icon = Icons.AutoMirrored.Outlined.ArrowBack,
                            small = true,
                            onPressed = { close() }
                        )
                    }
                    actions()
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        content()
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun Test() {

    TruvideoSdkTheme {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {

                Panel()
                {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.Red)
                    )
                }
            }
        }
    }
}