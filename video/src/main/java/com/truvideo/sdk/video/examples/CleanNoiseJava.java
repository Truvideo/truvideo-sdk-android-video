package com.truvideo.sdk.video.examples;

import androidx.annotation.NonNull;

import com.truvideo.sdk.video.TruvideoSdkVideo;
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoCallback;
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile;
import com.truvideo.sdk.video.model.TruvideoSdkVideoFileDescriptor;

import truvideo.sdk.common.exceptions.TruvideoSdkException;


public class CleanNoiseJava {
    public void cleanNoise(TruvideoSdkVideoFile input, TruvideoSdkVideoFileDescriptor output) {
        TruvideoSdkVideo.getInstance().clearNoise(
                input,
                output,
                new TruvideoSdkVideoCallback<String>() {
                    @Override
                    public void onComplete(String result) {
                        // Handle result
                        // Cleaned video is stored in resultVideoPath
                    }

                    @Override
                    public void onError(@NonNull TruvideoSdkException exception) {
                        // Handle error
                        exception.printStackTrace();
                    }
                }
        );
    }
}
