package com.example.educationappsysproject.videosection;

import android.content.Context;
import android.net.Uri;
import android.widget.MediaController;
import android.widget.VideoView;


/*
* implemented factory design pattern for showing video
* */
public class VideoComponentFactory {

    public static void setupVideoView(VideoView videoView, Uri videoUri) {
        videoView.setVideoURI(videoUri);
    }

    public static MediaController setupMediaController(Context context, VideoView videoView) {
        MediaController mediaController = new MediaController(context);
        mediaController.setMediaPlayer(videoView);
        videoView.setMediaController(mediaController);
        return mediaController;
    }
}
