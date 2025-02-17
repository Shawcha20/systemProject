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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

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
            FirebaseAuth auth = FirebaseAuth.getInstance();
            String userEmail = auth.getCurrentUser().getEmail(); // Get logged-in user's email

            if (courseId != null && userEmail != null) {
                // Reference to enrolled_users subcollection
                CollectionReference enrolledUsersRef = db.collection("course").document(courseId).collection("enrolled_users");

                // Add user to enrolled_users subcollection
                enrolledUsersRef.document(userEmail)
                        .set(new HashMap<>()) // Store an empty document
                        .addOnSuccessListener(aVoid -> {
                            // After enrolling, count the number of enrolled users
                            enrolledUsersRef.get().addOnSuccessListener(querySnapshot -> {
                                int enrolledCount = querySnapshot.size(); // Get total number of enrolled users

                                // Update the 'popular' field in the course document
                                db.collection("course").document(courseId)
                                        .update("popular", enrolledCount)
                                        .addOnSuccessListener(aVoid1 -> {
                                            Toast.makeText(course_description.this, "Enrolled Successfully!", Toast.LENGTH_SHORT).show();
                                            // Navigate to courseDetails
                                            Intent intent = new Intent(course_description.this, courseDetails.class);
                                            intent.putExtra("courseName", courseName);
                                            intent.putExtra("courseImage", courseImage);
                                            startActivity(intent);
                                            finish();
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(course_description.this, "Failed to update popularity!", Toast.LENGTH_SHORT).show());
                            });
                        })
                        .addOnFailureListener(e -> Toast.makeText(course_description.this, "Failed to enroll!", Toast.LENGTH_SHORT).show());
            }
        });

    }

}