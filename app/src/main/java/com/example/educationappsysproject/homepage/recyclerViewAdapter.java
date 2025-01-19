package com.example.educationappsysproject.homepage;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.educationappsysproject.R;
import com.example.educationappsysproject.videosection.VideoHome;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class recyclerViewAdapter extends RecyclerView.Adapter<recyclerViewAdapter.CourseViewHolder> {
    private Context context;
    private List<homeScreen.Course> courses; // Fixed: Use the correct Course class
    private FirebaseFirestore db;
    public recyclerViewAdapter(Context context, List<homeScreen.Course> courses) {
        this.context = context;
        this.courses = courses;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.course_card_view, parent, false); // Updated to use correct XML
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        homeScreen.Course course = courses.get(position);
        holder.title.setText(course.getTitle());
        Glide.with(context).load(course.getImageResourceId()).into(holder.image);

        db = FirebaseFirestore.getInstance();

        // Set click listener on the ImageView
        holder.image.setOnClickListener(v -> {
            // Fetch course document from Firestore
            db.collection("course")
                    .whereEqualTo("title", course.getTitle())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            // Get the document
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);

                            // Check if "enrolled" is true
                            boolean isEnrolled = document.getBoolean("enrolled");
                            if (isEnrolled) {
                                // Navigate to another activity if enrolled is true
                                Intent intent = new Intent(context, courseDetails.class);
                                intent.putExtra("courseName", course.getTitle());
                                intent.putExtra("courseImage", course.getImageResourceId());
                                context.startActivity(intent);
                            } else {
                                // Navigate to course_description activity if enrolled is false
                                Intent intent = new Intent(context, course_description.class);
                                intent.putExtra("courseName", course.getTitle());
                                intent.putExtra("courseImage", course.getImageResourceId());
                                context.startActivity(intent);
                            }
                        } else {
                            // Handle case where the document doesn't exist or fetch failed
                            Toast.makeText(context, "Failed to fetch course details.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }


    @Override
    public int getItemCount() {
        return courses.size();
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView image;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.courseTitle);
            image = itemView.findViewById(R.id.courseImage);
        }
    }
}
