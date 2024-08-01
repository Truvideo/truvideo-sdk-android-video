package com.truvideo.sdk.video.examples;

import androidx.annotation.NonNull;

import com.truvideo.sdk.video.TruvideoSdkVideo;
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoCallback;
import com.truvideo.sdk.video.model.TruvideoSdkVideoException;
import com.truvideo.sdk.video.model.TruvideoSdkVideoInformation;

public class GetVideoInfoJava {

    public void getVideoInfo(String videoPath) {
        TruvideoSdkVideo.getInstance().getInfo(videoPath, new TruvideoSdkVideoCallback<TruvideoSdkVideoInformation>() {
            @Override
            public void onComplete(@NonNull TruvideoSdkVideoInformation result) {
                // Handle video information
                int duration = result.getDurationMillis();
                int width = result.getWidth();
                int height = result.getHeight();
                String videoCodec = result.getVideoCodec();
                String audioCodec = result.getAudioCodec();
                int rotation = result.getRotation();
            }

            @Override
            public void onError(@NonNull TruvideoSdkVideoException exception) {
                // Handle error
            }
        });
    }
}
