package com.example.educationappsysproject.admin.addcourse_folder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.educationappsysproject.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class addCourseName extends AppCompatActivity {

    private Button gotoVideo;
    private EditText editTextCourseName, editTextDescription, editTextCatagory;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_course);

        // Initialize views
        gotoVideo = findViewById(R.id.going_to_video);
        editTextCourseName = findViewById(R.id.editTextCourseName);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextCatagory= findViewById(R.id.editTextCatagory);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Button click listener to save data and go to the next activity
        gotoVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get input data
                String courseName = editTextCourseName.getText().toString().trim();
                String description = editTextDescription.getText().toString().trim();
                String catagory= editTextCatagory.getText().toString().trim();
                // Validate input fields
                if (courseName.isEmpty() || description.isEmpty()) {
                    Toast.makeText(addCourseName.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    uploadCourseData(courseName, description, catagory);
                }
            }
        });
    }

    // Method to upload course data to Firestore
    private void uploadCourseData(String courseName, String description, String catagory) {
        // Create a map to store the data
        Map<String, Object> courseData = new HashMap<>();
        courseData.put("title", courseName);
        courseData.put("course_description", description);
        courseData.put("catagory", catagory);
        // Add the data to the "course" collection
        db.collection("course")
                .add(courseData)
                .addOnSuccessListener(documentReference -> {
                    // Get the document ID of the newly added course
                    String documentId = documentReference.getId();
                    Toast.makeText(addCourseName.this, "Course added successfully", Toast.LENGTH_SHORT).show();

                    // Pass the document ID to the next activity
                    goToNextActivity(documentId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(addCourseName.this, "Error adding course: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Method to navigate to the next activity
    private void goToNextActivity(String documentId) {
        Intent intent = new Intent(getApplicationContext(),uploadVideoPic.class);
        // Pass the document ID to the next activity
        intent.putExtra("documentId", documentId);
        startActivity(intent);
        finish();
    }
}
