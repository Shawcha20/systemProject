package com.example.educationappsysproject.admin.course;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.educationappsysproject.R;
import com.example.educationappsysproject.homepage.courseDetails;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class courseAdapter extends ArrayAdapter<String> {

    private Context context;
    private List<String> courseNames;
    private Map<String, String> courseIdMap; // Map to store course names and their document IDs

    public courseAdapter(Context context, List<String> courseNames) {
        super(context, R.layout.course_item, courseNames);
        this.context = context;
        this.courseNames = courseNames;
        this.courseIdMap = new HashMap<>();

        // Fetch document IDs for all course names
        fetchCourseDocumentIds();
    }

    private void fetchCourseDocumentIds() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("course")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        String courseName = document.getString("title"); // Adjust "title" to your field name
                        if (courseName != null) {
                            courseIdMap.put(courseName, document.getId());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error fetching course IDs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public View getView(int position, @Nullable View convertView, @Nullable ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.course_item, parent, false);
        }

        TextView courseNameText = convertView.findViewById(R.id.CourseUrlText);
        Button deleteButton = convertView.findViewById(R.id.courseDeleteButton);

        String courseName = courseNames.get(position);
        courseNameText.setText(courseName);

        // Set click listener for course name
        courseNameText.setOnClickListener(v -> {
            String documentId = courseIdMap.get(courseName);
            if (documentId != null) {
                Toast.makeText(context,courseName+"is selected",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, courseDetails.class);
                intent.putExtra("courseName", courseName);
                intent.putExtra("documentId", documentId);
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Document ID not found for this course.", Toast.LENGTH_SHORT).show();
            }
        });

        // Delete button listener
        deleteButton.setOnClickListener(v -> deleteCourse(position, courseName));

        return convertView;
    }

    private void deleteCourse(int position, String courseName) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Query the "course" collection to find the document with the matching title (courseName)
        firestore.collection("course")
                .whereEqualTo("title", courseName) // Adjust the field name to match your Firestore document structure
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Loop through the results and delete each document
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            String documentId = document.getId();

                            // Delete the document
                            firestore.collection("course")
                                    .document(documentId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(context, "Course deleted successfully.", Toast.LENGTH_SHORT).show();

                                        // Update the adapter's list and notify changes
                                        courseNames.remove(position);
                                        notifyDataSetChanged();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Error deleting course: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(context, "Course not found in Firestore.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error fetching course: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
