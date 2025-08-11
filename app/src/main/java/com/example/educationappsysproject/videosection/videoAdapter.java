package com.example.educationappsysproject.videosection;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class videoAdapter extends ArrayAdapter<String> {

    private Context context;
    private List<String> videoUrls;
    private String courseId;
    private FirebaseFirestore firestore;
    private boolean isAdmin = false; // To store access level

    public videoAdapter(Context context, List<String> videoUrls, String courseId) {
        super(context, R.layout.list_item_video, videoUrls);
        this.context = context;
        this.videoUrls = videoUrls;
        this.courseId = courseId;
        this.firestore = FirebaseFirestore.getInstance();

        // Check user access level when the adapter is created
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        checkUserAccessLevel(uid);
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

        videoUrlText.setOnClickListener(v -> {
            Intent intent = new Intent(context, ShowVideo.class);
            intent.putExtra("videoUrl", videoUrl);
            intent.putExtra("courseId", courseId);
            context.startActivity(intent);
        });

        // Set delete button visibility based on access level
        if (isAdmin) {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> deleteVideo(position, videoUrl));
        } else {
            deleteButton.setVisibility(View.GONE);
        }

        return convertView;
    }

    private void deleteVideo(int position, String videoUrl) {
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

    private void checkUserAccessLevel(String uid) {
        DocumentReference df = firestore.collection("users").document(uid);
        df.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot != null) {
                        Boolean checkLevel = documentSnapshot.getBoolean("checkLevel");
                        if (checkLevel != null && checkLevel) {
                            isAdmin = true; // User is admin
                        } else {
                            isAdmin = false; // User is not admin
                        }
                        notifyDataSetChanged(); // Update the UI
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching document: " + e.getMessage());
                    Toast.makeText(context, "Failed to verify access level. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }
}
