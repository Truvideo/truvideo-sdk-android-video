package com.truvideo.sdk.video.examples;

import androidx.annotation.NonNull;

import com.truvideo.sdk.video.TruvideoSdkVideo;
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoCallback;
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile;

import java.util.List;

import truvideo.sdk.common.exception.TruvideoSdkException;

public class CompareJava {

    public void compare(List<TruvideoSdkVideoFile> input) {
        TruvideoSdkVideo.getInstance().compare(input, new TruvideoSdkVideoCallback<Boolean>() {
            @Override
            public void onComplete(Boolean result) {
                // Handle result
                // result is true if the videos are compatible to be concatenated, otherwise false
            }

            @Override
            public void onError(@NonNull TruvideoSdkException exception) {
                // Handle error
                exception.printStackTrace();
            }
        });
    }
}
