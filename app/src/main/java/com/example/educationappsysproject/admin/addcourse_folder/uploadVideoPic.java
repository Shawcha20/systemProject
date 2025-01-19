package com.example.educationappsysproject.admin.addcourse_folder;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.educationappsysproject.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class uploadVideoPic extends AppCompatActivity {

    private Button uploadButton, selectPictureButton, selectVideoButton, goToQuestionButton,selectPdf;
    private ImageView imageViewPreview;
    private VideoView videoViewPreview;

    private Uri selectedImageUri, selectedVideoUri;

    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    private ProgressDialog progressDialog;

    private boolean imageUploaded = false, videoUploaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);

        // Initialize Firebase Storage
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        // Initialize Views
        selectPictureButton = findViewById(R.id.buttonSelectPicture);
        selectVideoButton = findViewById(R.id.selectVideo);
        uploadButton = findViewById(R.id.upload);
        goToQuestionButton = findViewById(R.id.goToQuestion);
        imageViewPreview = findViewById(R.id.imageViewPreview);
        videoViewPreview = findViewById(R.id.videoViewPreview);

        // Progress Dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);

        // Set Listeners for Selecting Media
        selectPictureButton.setOnClickListener(v -> selectPicture());
        selectVideoButton.setOnClickListener(v -> selectVideo());

        // Upload Button Listener
        uploadButton.setOnClickListener(v -> {
            if (selectedImageUri == null && selectedVideoUri == null) {
                Toast.makeText(this, "Please select an image or a video to upload.", Toast.LENGTH_SHORT).show();
                return;
            }
            uploadMediaToFirebase();
        });

        // Navigate to the next activity
        goToQuestionButton.setOnClickListener(v -> navigateToNextActivity());
    }

    private void selectPicture() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
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
            if (requestCode == 100) {
                selectedImageUri = data.getData();
                imageViewPreview.setImageURI(selectedImageUri);
                imageViewPreview.setVisibility(View.VISIBLE);
            } else if (requestCode == 101) {
                selectedVideoUri = data.getData();
                videoViewPreview.setVideoURI(selectedVideoUri);
                videoViewPreview.setVisibility(View.VISIBLE);
                videoViewPreview.start();
            }
        }
    }

    private void uploadMediaToFirebase() {
        progressDialog.show();

        String documentId = getIntent().getStringExtra("documentId");
        if (documentId == null) {
            Toast.makeText(this, "Document ID is missing.", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            return;
        }

        // Reset upload flags
        imageUploaded = false;
        videoUploaded = false;

        if (selectedImageUri != null) {
            // Upload Image
            String imageFileName = "images/" + UUID.randomUUID().toString();
            storageReference.child(imageFileName).putFile(selectedImageUri)
                    .addOnSuccessListener(imageTaskSnapshot -> {
                        // Get Image URL
                        storageReference.child(imageFileName).getDownloadUrl()
                                .addOnSuccessListener(imageUri -> {
                                    saveImageUrlToFirestore(documentId, imageUri.toString());
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error getting image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error uploading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            imageUploaded = true;
        }

        if (selectedVideoUri != null) {
            // Upload Video
            String videoName=UUID.randomUUID().toString();
            String videoFileName = "videos/" +videoName;
            storageReference.child(videoFileName).putFile(selectedVideoUri)
                    .addOnSuccessListener(videoTaskSnapshot -> {
                        // Get Video URL
                        storageReference.child(videoFileName).getDownloadUrl()
                                .addOnSuccessListener(videoUri -> {
                                    saveVideoToFirestore(documentId, videoUri.toString(), videoName);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error getting video URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error uploading video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            videoUploaded = true;
        }
    }

    private void saveImageUrlToFirestore(String documentId, String imageUrl) {
        FirebaseFirestore.getInstance()
                .collection("course")
                .document(documentId)
                .update("imageUrl", imageUrl)
                .addOnSuccessListener(aVoid -> {
                    imageUploaded = true;
                    checkUploadCompletion();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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
                    videoUploaded = true;
                    checkUploadCompletion();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving video metadata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkUploadCompletion() {
        if (imageUploaded && videoUploaded) {
            progressDialog.dismiss();
            Toast.makeText(this, "Upload completed successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToNextActivity() {
        String documentId = getIntent().getStringExtra("documentId");
        Intent intent = new Intent(uploadVideoPic.this, number_of_questions.class);
        intent.putExtra("documentId", documentId);
        startActivity(intent);
        finish();
    }
}
