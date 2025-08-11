package com.example.educationappsysproject.homepage.adapters;

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
import com.example.educationappsysproject.homepage.courseDetails;
import com.example.educationappsysproject.homepage.course_description;
import com.example.educationappsysproject.homepage.homeScreen;
import com.google.firebase.auth.FirebaseAuth;
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
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userEmail = auth.getCurrentUser().getEmail(); // Get logged-in user's email

        holder.image.setOnClickListener(v -> {
            db.collection("course")
                    .whereEqualTo("title", course.getTitle())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            String courseId = document.getId(); // Get the course document ID

                            // Check if user's email exists in enrolled_users subcollection
                            db.collection("course").document(courseId)
                                    .collection("enrolled_users")
                                    .document(userEmail) // Check if this document exists
                                    .get()
                                    .addOnCompleteListener(userTask -> {
                                        if (userTask.isSuccessful() && userTask.getResult().exists()) {
                                            // User is enrolled, navigate to courseDetails
                                            Intent intent = new Intent(context, courseDetails.class);
                                            intent.putExtra("courseName", course.getTitle());
                                            intent.putExtra("courseImage", course.getImageResourceId());
                                            context.startActivity(intent);
                                        } else {
                                            // User is not enrolled, navigate to course_description
                                            Intent intent = new Intent(context, course_description.class);
                                            intent.putExtra("courseName", course.getTitle());
                                            intent.putExtra("courseImage", course.getImageResourceId());
                                            context.startActivity(intent);
                                        }
                                    });
                        } else {
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
