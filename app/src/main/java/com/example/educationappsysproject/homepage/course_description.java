package com.example.educationappsysproject.homepage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.educationappsysproject.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class course_description extends AppCompatActivity {


    private TextView courseTitle,courseDescription;
    private Button enrollButton;
    FirebaseFirestore db;
    private String courseId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_course_description);
        // Initialize views
        courseTitle = findViewById(R.id.courseTitle);
        courseDescription = findViewById(R.id.courseDescription);
        enrollButton = findViewById(R.id.enrollButton);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get data from the Intent
        String courseName = getIntent().getStringExtra("courseName");
        String courseImage=getIntent().getStringExtra("courseImage");
        // Fetch course details from Firebase
        db.collection("course")
                .whereEqualTo("title", courseName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        courseId = document.getId(); // Store the document ID for updating
                        courseTitle.setText(document.getString("title"));
                        courseDescription.setText(document.getString("course_description"));
                    } else {
                        Toast.makeText(this, "Course not found!", Toast.LENGTH_SHORT).show();
                    }
                });

        // Handle Enroll button click
        enrollButton.setOnClickListener(v -> {
            if (courseId != null) {
                // Update the 'enrolled' field in Firebase
                db.collection("course").document(courseId)
                        .update("enrolled", true)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Enrolled Successfully!", Toast.LENGTH_SHORT).show();
                            // Navigate to the next activity
                            Intent intent = new Intent(course_description.this, courseDetails.class);
                            intent.putExtra("courseName",courseName);
                            intent.putExtra("courseImage", courseImage);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to enroll!", Toast.LENGTH_SHORT).show());
            }
        });
    }

}