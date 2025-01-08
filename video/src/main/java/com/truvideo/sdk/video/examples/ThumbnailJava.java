package com.truvideo.sdk.video.examples;

import androidx.annotation.NonNull;

import com.truvideo.sdk.video.TruvideoSdkVideo;
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoCallback;
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile;
import com.truvideo.sdk.video.model.TruvideoSdkVideoFileDescriptor;

import truvideo.sdk.common.exceptions.TruvideoSdkException;

public class ThumbnailJava {
    public void createThumbnail(TruvideoSdkVideoFile input, TruvideoSdkVideoFileDescriptor output) {
        TruvideoSdkVideo.getInstance().createThumbnail(
                input,
                output,
                1000, // position
                300, // width, can be null
                300, // height, can be null
                false, // precise
                new TruvideoSdkVideoCallback<String>() {
                    @Override
                    public void onComplete(@NonNull String resultPath) {
                        // Handle result
                        // the thumbnail is stored at resultPath
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
