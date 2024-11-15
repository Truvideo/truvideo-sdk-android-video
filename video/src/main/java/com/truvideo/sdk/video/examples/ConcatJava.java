package com.truvideo.sdk.video.examples;

import androidx.annotation.NonNull;

import com.truvideo.sdk.video.TruvideoSdkVideo;
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoCallback;
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile;
import com.truvideo.sdk.video.model.TruvideoSdkVideoFileDescriptor;
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequest;
import com.truvideo.sdk.video.video_request_builder.TruvideoSdkVideoConcatBuilder;

import java.util.List;

import truvideo.sdk.common.exceptions.TruvideoSdkException;

public class ConcatJava {

    public void concatVideos(List<TruvideoSdkVideoFile> input, TruvideoSdkVideoFileDescriptor output) {
        TruvideoSdkVideoConcatBuilder builder = TruvideoSdkVideo.getInstance().ConcatBuilder(input, output);
        builder.build(new TruvideoSdkVideoCallback<TruvideoSdkVideoRequest>() {
            @Override
            public void onComplete(@NonNull TruvideoSdkVideoRequest request) {
                // Request ready

                // Send to process
                request.process(new TruvideoSdkVideoCallback<String>() {
                    @Override
                    public void onComplete(String resultPath) {
                        // Handle result
                        // the concated video its on 'resultPath'
                    }

                    @Override
                    public void onError(@NonNull TruvideoSdkException exception) {
                        // Handle error concating videos
                        exception.printStackTrace();
                    }
                });
            }

            @Override
            public void onError(@NonNull TruvideoSdkException exception) {
                // Handle error creating request
                exception.printStackTrace();
            }
        });
    }
}
