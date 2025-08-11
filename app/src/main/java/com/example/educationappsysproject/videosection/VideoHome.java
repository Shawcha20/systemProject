package com.example.educationappsysproject.videosection;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.educationappsysproject.Authentication.login;
import com.example.educationappsysproject.R;
import com.example.educationappsysproject.admin.adminHomePage;
import com.example.educationappsysproject.admin.editCourse.updateVideo;
import com.example.educationappsysproject.homepage.homeScreen;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
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
    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_home);

        // Initialize Firebase Auth
        fAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = fAuth.getCurrentUser();

        videoListView = findViewById(R.id.videoListView);
        firestore = FirebaseFirestore.getInstance();
        courseTitle = getIntent().getStringExtra("courseName");
        addVideo = findViewById(R.id.addVideoButton);

        if (currentUser != null) {
            // User is logged in, check their access level
            checkUserAccessLevel(currentUser.getUid());
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }

        if (courseTitle == null || courseTitle.isEmpty()) {
            Toast.makeText(this, "Course title is missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchCourseAndVideos();

        addVideo.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), updateVideo.class);
            intent.putExtra("courseName", courseTitle);
            Toast.makeText(VideoHome.this, "Going to updateVideo", Toast.LENGTH_SHORT).show();
            startActivity(intent);
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

    private void checkUserAccessLevel(String uid) {
        // Fetch user document from Firestore
        DocumentReference df = firestore.collection("users").document(uid);
        df.get()
                .addOnSuccessListener(documentSnapshot -> {
                    Log.d(TAG, "onSuccess: " + documentSnapshot.getData());
                    Boolean checkLevel = documentSnapshot.getBoolean("checkLevel");

                    if (checkLevel != null && checkLevel) {
                        // Admin user
                        addVideo.setVisibility(View.VISIBLE);
                        Toast.makeText(VideoHome.this, "Admin access granted. Button is visible.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Regular user
                        addVideo.setVisibility(View.GONE);
                        Toast.makeText(VideoHome.this, "Regular user access. Button is hidden.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching document: " + e.getMessage());
                    Toast.makeText(VideoHome.this, "Failed to verify access level. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }
}