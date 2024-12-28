package com.example.educationappsysproject.videosection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.ArrayAdapter;

import com.example.educationappsysproject.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class videoAdapter extends ArrayAdapter<String> {

    private Context context;
    private List<String> videoUrls;
    private String courseId;

    public videoAdapter(Context context, List<String> videoUrls, String courseId) {
        super(context, R.layout.list_item_video, videoUrls);
        this.context = context;
        this.videoUrls = videoUrls;
        this.courseId = courseId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_video, parent, false);
        }

        TextView videoUrlText = convertView.findViewById(R.id.videoUrlText);
        Button deleteButton = convertView.findViewById(R.id.deleteButton);

        String videoUrl = videoUrls.get(position);
        videoUrlText.setText(videoUrl);

        // Delete button listener
        deleteButton.setOnClickListener(v -> deleteVideo(position, videoUrl));

        return convertView;
    }

    private void deleteVideo(int position, String videoUrl) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Query the "videos" subcollection to find and delete the document with the matching videoUrl
        firestore.collection("course")
                .document(courseId)
                .collection("videos")
                .whereEqualTo("videoUrl", videoUrl)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        videoUrls.remove(position);
                                        notifyDataSetChanged();
                                        Toast.makeText(context, "Video deleted successfully.", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Error deleting video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(context, "Video not found in Firestore.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error fetching video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
