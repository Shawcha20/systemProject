package com.example.educationappsysproject.admin.editCourse;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.educationappsysproject.R;
import com.example.educationappsysproject.videosection.VideoHome;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class updateVideo extends AppCompatActivity {
    private Button uploadButton, selectVideoButton;
    private VideoView videoViewPreview;
    private Uri selectedVideoUri;

    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;
    private String courseTitle;
    private String courseId; // To store the retrieved document ID (courseId)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_video);

        // Initialize Firebase Storage
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        // Initialize Views
        selectVideoButton = findViewById(R.id.updateVIdeo);
        uploadButton = findViewById(R.id.upDate);
        videoViewPreview = findViewById(R.id.UvideoViewPreview);

        // Progress Dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);

        // Get courseTitle from the intent
         courseTitle = getIntent().getStringExtra("courseName");
        if (courseTitle == null || courseTitle.isEmpty()) {
            Toast.makeText(this, "Course title is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieve the document ID (courseId) based on the courseTitle
        fetchCourseId(courseTitle);

        // Set Listeners for Selecting Media
        selectVideoButton.setOnClickListener(v -> selectVideo());

        // Upload Button Listener
        uploadButton.setOnClickListener(v -> {
            if (selectedVideoUri == null) {
                Toast.makeText(this, "Please select a video to upload.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (courseId == null) {
                Toast.makeText(this, "Course ID is not available. Try again.", Toast.LENGTH_SHORT).show();
                return;
            }
            uploadMediaToFirebase();
        });
    }

    private void fetchCourseId(String courseTitle) {
        FirebaseFirestore.getInstance()
                .collection("course")
                .whereEqualTo("title", courseTitle)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        courseId = querySnapshot.getDocuments().get(0).getId();
                    } else {
                        Toast.makeText(this, "No course found with the given title.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching course ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void selectVideo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == 101) {
                selectedVideoUri = data.getData();
                videoViewPreview.setVideoURI(selectedVideoUri);
                videoViewPreview.setVisibility(View.VISIBLE);
                videoViewPreview.start();
            }
        }
    }

    private void uploadMediaToFirebase() {
        progressDialog.show();

        if (selectedVideoUri != null) {
            // Upload Video
            String videoName = UUID.randomUUID().toString();
            String videoFileName = "videos/" + videoName;
            storageReference.child(videoFileName).putFile(selectedVideoUri)
                    .addOnSuccessListener(videoTaskSnapshot -> {
                        // Get Video URL
                        storageReference.child(videoFileName).getDownloadUrl()
                                .addOnSuccessListener(videoUri -> {
                                    saveVideoToFirestore(courseId, videoUri.toString(), videoName);
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(this, "Error getting video URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Error uploading video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            progressDialog.dismiss();
            Toast.makeText(this, "No video selected for upload.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveVideoToFirestore(String documentId, String videoUrl, String videoName) {
        Map<String, Object> videoData = new HashMap<>();
        videoData.put("videoName", videoName);
        videoData.put("videoUrl", videoUrl);

        FirebaseFirestore.getInstance()
                .collection("course")
                .document(documentId)
                .collection("videos") // Add a "videos" subcollection
                .add(videoData) // Add the video data to the subcollection
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Upload completed successfully!", Toast.LENGTH_SHORT).show();
                    navigateToNextActivity(); // Navigate to the next activity here
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Error saving video metadata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToNextActivity() {
        Intent intent = new Intent(getApplicationContext(), VideoHome.class);
        intent.putExtra("courseName", courseTitle);
        startActivity(intent);
        finish();
    }
}
