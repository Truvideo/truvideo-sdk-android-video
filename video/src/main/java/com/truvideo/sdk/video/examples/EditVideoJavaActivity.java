package com.truvideo.sdk.video.examples;

import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.truvideo.sdk.video.TruvideoSdkVideo;
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoCallback;
import com.truvideo.sdk.video.model.TruvideoSdkVideoException;
import com.truvideo.sdk.video.usecases.TruvideoSdkVideoEditor;


public class EditVideoJavaActivity extends ComponentActivity {

    private TruvideoSdkVideoEditor videoEditor = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        videoEditor = TruvideoSdkVideo.getInstance().initEditor(this);
    }

    private void editVideo(String videoPath, String resultPath) {
        if (videoEditor == null) return;

        videoEditor.edit(videoPath, resultPath, new TruvideoSdkVideoCallback<String>() {
            @Override
            public void onComplete(@Nullable String path) {
                // Handle result
                // if path its null means the user canceled the edition
            }

            @Override
            public void onError(@NonNull TruvideoSdkVideoException exception) {
                // Handle error
                exception.printStackTrace();
            }
        });
    }
}
