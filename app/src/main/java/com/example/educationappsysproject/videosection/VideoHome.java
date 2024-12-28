package com.example.educationappsysproject.videosection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.educationappsysproject.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class VideoHome extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private String courseTitle;
    private String courseId;
    private Button addVideo;
    private ListView videoListView;
    private List<String> videoUrls = new ArrayList<>();
    private videoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_home);

        videoListView = findViewById(R.id.videoListView);
        firestore = FirebaseFirestore.getInstance();
        courseTitle = getIntent().getStringExtra("courseName");
        addVideo= findViewById(R.id.addVideoButton);

        if (courseTitle == null || courseTitle.isEmpty()) {
            Toast.makeText(this, "Course title is missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        fetchCourseAndVideos();

        addVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(getApplicationContext(), updateVideo.class);
                Toast.makeText(VideoHome.this,"going to updateVideo",Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
        });

    }

    private void fetchCourseAndVideos() {
        firestore.collection("course")
                .whereEqualTo("title", courseTitle)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot courseDocument = querySnapshot.getDocuments().get(0);
                        courseId = courseDocument.getId();

                        fetchVideosFromSubcollection(courseId);
                    } else {
                        Toast.makeText(this, "No course found with the title.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching course: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchVideosFromSubcollection(String courseId) {
        firestore.collection("course")
                .document(courseId)
                .collection("videos")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        videoUrls.clear();
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            String videoUrl = document.getString("videoUrl");
                            if (videoUrl != null) {
                                videoUrls.add(videoUrl);
                            }
                        }
                        adapter = new videoAdapter(VideoHome.this, videoUrls, courseId);
                        videoListView.setAdapter(adapter);
                    } else {
                        Toast.makeText(this, "No videos found for this course.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching videos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
