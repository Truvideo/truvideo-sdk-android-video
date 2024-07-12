package com.truvideo.sdk.video.examples;

import androidx.annotation.NonNull;

import com.truvideo.sdk.video.TruvideoSdkVideo;
import com.truvideo.sdk.video.interfaces.TruvideoSdkVideoCallback;
import com.truvideo.sdk.video.model.TruvideoSdkVideoException;

import java.util.List;

public class CompareJava {

    public void compare(List<String> videoPaths) {
        TruvideoSdkVideo.getInstance().compare(videoPaths, new TruvideoSdkVideoCallback<Boolean>() {
            @Override
            public void onComplete(Boolean result) {
                // Handle result
            }

            @Override
            public void onError(@NonNull TruvideoSdkVideoException exception) {
                // Handle error
                exception.printStackTrace();
            }
        });
    }
}
