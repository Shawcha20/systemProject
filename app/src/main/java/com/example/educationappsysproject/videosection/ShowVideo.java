package com.example.educationappsysproject.videosection;

import androidx.appcompat.app.AppCompatActivity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.VideoView;

import com.example.educationappsysproject.R;

public class ShowVideo extends AppCompatActivity {

    private VideoView showVideo;

    // Correct public Firebase URL with access token
    private String url1 = "https://firebasestorage.googleapis.com/v0/b/aspireacademy-software-project.appspot.com/o/Videos%2FintegrationClass1.mp4?alt=media&token=215078d0-fa3f-416c-b0bd-5971e1048fe4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_video);

        showVideo = findViewById(R.id.videoView);

        // Parse the Firebase URL and set up VideoView
        Log.d("video url", url1);
        Uri uri = Uri.parse(url1);
        Log.d("parsed",uri.toString());
        VideoComponentFactory.setupVideoView(showVideo, uri);
        VideoComponentFactory.setupMediaController(this, showVideo);

        // Start playback
        showVideo.requestFocus();
        showVideo.start();
    }
}
