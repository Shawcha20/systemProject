//UserAdapter
package com.example.educationappsysproject.homepage.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.educationappsysproject.R;
import com.example.educationappsysproject.homepage.chat.ChatActivity;
import com.example.educationappsysproject.homepage.models.Users;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context context;
    private List<Users> usersList;
    private String currentUserId;

    public UserAdapter(Context context, List<Users> usersList, String currentUserId) {
        this.context = context;
        this.usersList = usersList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users user = usersList.get(position);
        
        // Set user info
        holder.userName.setText(user.getName());
        holder.userEmail.setText(user.getEmail());
        holder.userStudentId.setText(user.getStudentId());
        
        // Check if user is admin and show label
        if (user.isAdmin()) {
            holder.adminLabel.setVisibility(View.VISIBLE);
            holder.userName.setText(user.getName() + " (Admin)");
        } else {
            holder.adminLabel.setVisibility(View.GONE);
        }
        
        // Load profile image
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(user.getProfileImageUrl())
                    .placeholder(R.drawable.user)
                    .error(R.drawable.user)
                    .circleCrop()
                    .into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.user);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);

            intent.putExtra("userId", user.getUserId());
            intent.putExtra("userName", user.getName());
            intent.putExtra("isAdmin", user.isAdmin());
            context.startActivity(intent);
            // Suppress transition animation properly
            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).overridePendingTransition(0, 0);
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userEmail, userStudentId, adminLabel;
        ImageView profileImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            userEmail = itemView.findViewById(R.id.user_email);
            userStudentId = itemView.findViewById(R.id.user_student_id);
            adminLabel = itemView.findViewById(R.id.admin_label);
            profileImage = itemView.findViewById(R.id.profile_image);
        }
    }
}
