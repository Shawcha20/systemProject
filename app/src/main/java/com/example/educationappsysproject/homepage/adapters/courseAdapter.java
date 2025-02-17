package com.example.educationappsysproject.homepage.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.educationappsysproject.R;

import java.util.List;

public class courseAdapter extends RecyclerView.Adapter<courseAdapter.CourseViewHolder> {

    private List<String> courseList;

    public courseAdapter(List<String> courseList) {
        this.courseList = courseList;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_home_screen, parent, false); // Use your CardView layout file here
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        String courseName = courseList.get(position);
        holder.courseNameTextView.setText(courseName);
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView courseNameTextView;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
         //   courseNameTextView = itemView.findViewById(R.id.courseNameText); // TextView inside your CardView
        }
    }
}
