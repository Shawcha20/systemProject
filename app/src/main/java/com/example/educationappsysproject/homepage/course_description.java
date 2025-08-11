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
import java.util.Map;

public class course_description extends AppCompatActivity {

    private TextView courseTitle, courseDescription;
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
        String courseImage = getIntent().getStringExtra("courseImage");
        
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
            String userId = auth.getCurrentUser().getUid(); // Get user ID to update user doc

            if (courseId != null && userEmail != null) {
                CollectionReference enrolledUsersRef = db.collection("course").document(courseId).collection("enrolled_users");

                // Enroll user under course's enrolled_users
                enrolledUsersRef.document(userEmail)
                        .set(new HashMap<>()) // Empty document
                        .addOnSuccessListener(aVoid -> {
                            // Step 1: Update popularity count
                            enrolledUsersRef.get().addOnSuccessListener(querySnapshot -> {
                                int enrolledCount = querySnapshot.size();
                                db.collection("course").document(courseId)
                                        .update("popular", enrolledCount)
                                        .addOnSuccessListener(aVoid1 -> {
                                            // Step 2: Add course name to user's enrolled_courses subcollection
                                            HashMap<String, Object> courseData = new HashMap<>();
                                            courseData.put("courseName", courseName);
                                            courseData.put("courseId", courseId);

                                            db.collection("users")
                                                    .document(userId)
                                                    .collection("enrolled_courses")
                                                    .document(courseId)
                                                    .set(courseData)
                                                    .addOnSuccessListener(aVoid2 -> {
                                                        // Step 3: Send notification to admin
                                                        notifyAdmin(userEmail, courseName);
                                                        
                                                        Toast.makeText(course_description.this, "Enrolled Successfully!", Toast.LENGTH_SHORT).show();
                                                        // Navigate to courseDetails
                                                        Intent intent = new Intent(course_description.this, courseDetails.class);
                                                        intent.putExtra("courseName", courseName);
                                                        intent.putExtra("courseImage", courseImage);
                                                        startActivity(intent);
                                                        finish();
                                                    })
                                                    .addOnFailureListener(e -> Toast.makeText(course_description.this, "Failed to update user's enrolled courses!", Toast.LENGTH_SHORT).show());

                                        })
                                        .addOnFailureListener(e -> Toast.makeText(course_description.this, "Failed to update popularity!", Toast.LENGTH_SHORT).show());
                            });
                        })
                        .addOnFailureListener(e -> Toast.makeText(course_description.this, "Failed to enroll!", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void notifyAdmin(String userEmail, String courseName) {
        // Find admin users and send them a notification
        db.collection("users")
                .whereEqualTo("checkLevel", true) // Admin users
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot adminDoc : querySnapshot) {
                        String adminId = adminDoc.getId();
                        
                        // Create notification data
                        Map<String, Object> notification = new HashMap<>();
                        notification.put("type", "enrollment");
                        notification.put("message", "User " + userEmail + " enrolled in course: " + courseName);
                        notification.put("timestamp", System.currentTimeMillis());
                        notification.put("courseName", courseName);
                        notification.put("userEmail", userEmail);
                        notification.put("read", false);
                        
                        // Add notification to admin's notifications collection
                        db.collection("users")
                                .document(adminId)
                                .collection("notifications")
                                .add(notification)
                                .addOnSuccessListener(documentReference -> {
                                    // Notification sent successfully
                                })
                                .addOnFailureListener(e -> {
                                    // Handle notification failure
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle admin query failure
                });
    }
}