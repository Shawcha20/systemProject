package com.example.educationappsysproject.admin.addcourse_folder;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.educationappsysproject.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class number_of_questions extends AppCompatActivity {

    private Button next, buttonExamPic;
    private EditText numQuestion, examTitle;
    private FirebaseFirestore db;
    private String documentId;
    private ImageView imageViewPreview;
    private Uri selectedImageUri;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number_of_questions);

        // Initialize Views
        numQuestion = findViewById(R.id.question_number_input);
        examTitle = findViewById(R.id.examTitleInput);
        next = findViewById(R.id.next_button);
        buttonExamPic = findViewById(R.id.buttonExamPic);
        imageViewPreview = findViewById(R.id.examPicViewPreview);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        // Get the parent course document ID
        documentId = getIntent().getStringExtra("documentId");

        if (documentId == null) {
            Toast.makeText(this, "Course ID not found. Exiting...", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);

        // Set Click Listeners
        buttonExamPic.setOnClickListener(v -> selectPicture());

        next.setOnClickListener(v -> {
            String numQuestionText = numQuestion.getText().toString().trim();
            String title = examTitle.getText().toString().trim();

            if (!numQuestionText.isEmpty() && !title.isEmpty()) {
                try {
                    int examNum = Integer.parseInt(numQuestionText);
                    uploadpic(examNum, title);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectPicture() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == 100) {
                selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    imageViewPreview.setImageURI(selectedImageUri);
                    imageViewPreview.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(this, "Failed to select image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private void uploadpic(int examNum, String title) {
        progressDialog.show();

        if (selectedImageUri != null) {
            // Upload Image
            String imageFileName = "images/" + UUID.randomUUID().toString();
            storageReference.child(imageFileName).putFile(selectedImageUri)
                    .addOnSuccessListener(imageTaskSnapshot -> {
                        // Get Image URL
                        storageReference.child(imageFileName).getDownloadUrl()
                                .addOnSuccessListener(imageUri -> {
                                    saveExamDetails(imageUri.toString(), examNum, title);
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(this, "Error getting image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Error uploading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // No image selected, proceed with other details
            saveExamDetails(null, examNum, title);
        }
    }

    private void saveExamDetails(String imageUrl, int examNum, String title) {
        Map<String, Object> examDetails = new HashMap<>();
        examDetails.put("examTitle", title);
        examDetails.put("num", examNum);
        if (imageUrl != null) {
            examDetails.put("examImage", imageUrl);
        }

        db.collection("course")
                .document(documentId)
                .collection("exam")
                .add(examDetails)
                .addOnSuccessListener(documentReference -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Exam added successfully!", Toast.LENGTH_SHORT).show();

                    // Navigate to next activity
                    Intent intent = new Intent(this, uploadingQuestions.class);
                    intent.putExtra("documentId", documentId);
                    intent.putExtra("examDocumentId", documentReference.getId());
                    intent.putExtra("Number", examNum);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to add exam: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
