package com.truvideo.sdk.video.examples;

import androidx.annotation.NonNull;

import com.truvideo.sdk.video.TruvideoSdkVideo;
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoCallback;
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile;
import com.truvideo.sdk.video.model.TruvideoSdkVideoInformation;

import truvideo.sdk.common.exception.TruvideoSdkException;

public class GetVideoInfoJava {

    public void getVideoInfo(TruvideoSdkVideoFile input) {
        TruvideoSdkVideo.getInstance().getInfo(input, new TruvideoSdkVideoCallback<TruvideoSdkVideoInformation>() {
            @Override
            public void onComplete(@NonNull TruvideoSdkVideoInformation result) {
                // Handle video information
            }

            @Override
            public void onError(@NonNull TruvideoSdkException exception) {
                // Handle error
            }
        });
    }
}
