package com.truvideo.sdk.video.examples;

import androidx.annotation.NonNull;

import com.truvideo.sdk.video.TruvideoSdkVideo;
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoCallback;
import com.truvideo.sdk.video.model.TruvideoSdkVideoException;

public class ThumbnailJava {
    public void createThumbnail(String videoPath, String resultImagePath) {
        TruvideoSdkVideo.getInstance().createThumbnail(
                videoPath,
                resultImagePath,
                1000, // position
                300, // width, can be null
                300, // height, can be null
                new TruvideoSdkVideoCallback<String>() {
                    @Override
                    public void onComplete(@NonNull String path) {
                        // Handle result
                        // the thumbnail is stored at resultImagePath
                    }

                    @Override
                    public void onError(@NonNull TruvideoSdkVideoException exception) {
                        // Handle error
                        exception.printStackTrace();
                    }
                }
        );
    }
}
