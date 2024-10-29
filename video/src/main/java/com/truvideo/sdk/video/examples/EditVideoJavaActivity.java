package com.truvideo.sdk.video.examples;

import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;

import com.truvideo.sdk.video.model.TruvideoSdkVideoFile;
import com.truvideo.sdk.video.model.TruvideoSdkVideoFileDescriptor;
import com.truvideo.sdk.video.ui.activities.edit.TruvideoSdkVideoEditContract;
import com.truvideo.sdk.video.ui.activities.edit.TruvideoSdkVideoEditParams;

public class EditVideoJavaActivity extends ComponentActivity {

    private ActivityResultLauncher<TruvideoSdkVideoEditParams> editVideoLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editVideoLauncher = registerForActivityResult(new TruvideoSdkVideoEditContract(), path -> {
            // edited video its on 'path'
        });
    }

    private void editVideo(TruvideoSdkVideoFile input, TruvideoSdkVideoFileDescriptor output) {
        editVideoLauncher.launch(new TruvideoSdkVideoEditParams(input, output));
    }
}
