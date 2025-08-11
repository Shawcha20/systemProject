package com.example.educationappsysproject.homepage.adapters;



import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.educationappsysproject.R;
import com.example.educationappsysproject.homepage.models.Message;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Message> messageList;

    private static final int USER = 0;
    private static final int BOT = 1;

    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).isUser() ? USER : BOT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == USER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_message, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bot_message, parent, false);
            return new BotViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).userText.setText(message.getContent());
        } else {
            ((BotViewHolder) holder).botText.setText(message.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userText;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userText = itemView.findViewById(R.id.userTextView);
        }
    }

    static class BotViewHolder extends RecyclerView.ViewHolder {
        TextView botText;
        public BotViewHolder(@NonNull View itemView) {
            super(itemView);
            botText = itemView.findViewById(R.id.botTextView);
 }
}
}
