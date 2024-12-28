package com.example.educationappsysproject.videosection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.educationappsysproject.R;
import com.example.educationappsysproject.videosection.VideoComponentFactory;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ShowVideo extends AppCompatActivity {

    private VideoView showVideo;
    private FirebaseFirestore firestore;
    private String courseTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_video);

        // Initialize Firestore and Views
        firestore = FirebaseFirestore.getInstance();
        showVideo = findViewById(R.id.videoView);

        // Get course title from the intent
        courseTitle = getIntent().getStringExtra("courseName");
     //   Toast.makeText(ShowVideo.this,courseTitle,Toast.LENGTH_SHORT).show();
        if (courseTitle == null || courseTitle.isEmpty()) {
            Toast.makeText(this, "Course title is missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Fetch video URL for the given course title
        fetchVideoUrl(courseTitle);
    }

    private void fetchVideoUrl(String title) {
        firestore.collection("course")
                .whereEqualTo("title", title)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Get the first document that matches the query
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        String videoUrl = document.getString("videoUrl");

                        if (videoUrl != null && !videoUrl.isEmpty()) {
                            playVideo(videoUrl);
                        } else {
                            Toast.makeText(this, "Video URL not found for this course.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "No course found with the given title.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Error", "Error fetching video URL: " + e.getMessage());
                    Toast.makeText(this, "Error fetching video URL.", Toast.LENGTH_SHORT).show();
                });
    }

    private void playVideo(String url) {
        Log.d("Video URL", url);
        Uri uri = Uri.parse(url);

        // Use VideoComponentFactory for setup (assuming it's a utility you have created)
        VideoComponentFactory.setupVideoView(showVideo, uri);
        VideoComponentFactory.setupMediaController(this, showVideo);

        // Start playback
        showVideo.requestFocus();
        showVideo.start();
    }
}
