package com.truvideo.sdk.video.examples;

import androidx.annotation.NonNull;

import com.truvideo.sdk.video.TruvideoSdkVideo;
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoCallback;
import com.truvideo.sdk.video.model.TruvideoSdkVideoException;
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequest;
import com.truvideo.sdk.video.video_request_builder.TruvideoSdkVideoMergeBuilder;

import java.util.List;

import kotlin.Unit;

public class MergeJava {
    public void mergeVideos(List<String> videos, String resultVideoPath) {
        TruvideoSdkVideoMergeBuilder builder = TruvideoSdkVideo.getInstance().MergeBuilder(videos, resultVideoPath);
        builder.build(new TruvideoSdkVideoCallback<TruvideoSdkVideoRequest>() {
            @Override
            public void onComplete(@NonNull TruvideoSdkVideoRequest request) {
                // Request ready

                // Send to process
                request.process(new TruvideoSdkVideoCallback<Unit>() {
                    @Override
                    public void onComplete(Unit result) {
                        // Handle result
                        // the merged video its on 'resultVideoPath'
                    }

                    @Override
                    public void onError(@NonNull TruvideoSdkVideoException exception) {
                        // Handle error merging the videos
                        exception.printStackTrace();
                    }
                });
            }

            @Override
            public void onError(@NonNull TruvideoSdkVideoException exception) {
                // Handle error creating the request
                exception.printStackTrace();
            }
        });
    }
}
