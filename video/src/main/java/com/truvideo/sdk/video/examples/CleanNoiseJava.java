package com.truvideo.sdk.video.examples;


import androidx.annotation.NonNull;

import com.truvideo.sdk.video.TruvideoSdkVideo;
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoCallback;
import com.truvideo.sdk.video.model.TruvideoSdkVideoException;

public class CleanNoiseJava {
    public void cleanNoise(String videoPath, String resultVideoPath) {
        TruvideoSdkVideo.getInstance().clearNoise(videoPath, resultVideoPath, new TruvideoSdkVideoCallback<String>() {
            @Override
            public void onComplete(String result) {
                // Handle result
                // Cleaned video is stored in resultVideoPath
            }

            @Override
            public void onError(@NonNull TruvideoSdkVideoException exception) {
                exception.printStackTrace();
            }
        });
    }
}
