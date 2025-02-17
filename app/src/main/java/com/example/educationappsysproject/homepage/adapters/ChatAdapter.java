package com.example.educationappsysproject.homepage.adapters;




import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.educationappsysproject.R;
import com.example.educationappsysproject.homepage.models.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private Context context;
    private List<ChatMessage> chatMessages;
    private String currentUserId;

    public ChatAdapter(Context context, List<ChatMessage> chatMessages, String currentUserId) {
        this.context = context;
        this.chatMessages = chatMessages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == VIEW_TYPE_SENT) {
            View view = inflater.inflate(R.layout.item_sent_message, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_received_message, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);

        // Determine if we need to show the date
        boolean showDate = shouldShowDate(position);

        if (holder instanceof SentMessageViewHolder) {
            SentMessageViewHolder sentHolder = (SentMessageViewHolder) holder;
            sentHolder.messageText.setText(message.getMessage());
            sentHolder.messageTime.setText(formatTimestamp(message.getTimestamp()));

            // Show date if needed
            if (showDate) {
                sentHolder.messageDate.setVisibility(View.VISIBLE);
                sentHolder.messageDate.setText(formatDate(message.getTimestamp()));
            } else {
                sentHolder.messageDate.setVisibility(View.GONE);
            }
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ReceivedMessageViewHolder receivedHolder = (ReceivedMessageViewHolder) holder;
            receivedHolder.messageText.setText(message.getMessage());
            receivedHolder.messageTime.setText(formatTimestamp(message.getTimestamp()));

            // Show date if needed
            if (showDate) {
                receivedHolder.messageDate.setVisibility(View.VISIBLE);
                receivedHolder.messageDate.setText(formatDate(message.getTimestamp()));
            } else {
                receivedHolder.messageDate.setVisibility(View.GONE);
            }
        }
    }

    // Check if we should show the date
    private boolean shouldShowDate(int position) {
        if (position == 0) return true; // Always show date for first message

        ChatMessage currentMessage = chatMessages.get(position);
        ChatMessage previousMessage = chatMessages.get(position - 1);

        // Compare dates
        return !isSameDay(currentMessage.getTimestamp(), previousMessage.getTimestamp());
    }

    // Enhanced date formatting method
    private String formatDate(Long timestamp) {
        Calendar messageCalendar = Calendar.getInstance();
        messageCalendar.setTimeInMillis(timestamp);

        Calendar today = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);

        // Check if the message is from today
        if (isSameDay(messageCalendar, today)) {
            return "Today";
        }
        // Check if the message is from yesterday
        else if (isSameDay(messageCalendar, yesterday)) {
            return "Yesterday";
        }
        // For older dates, use standard date format
        else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }

    // Helper method to check if two calendars represent the same day
    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    // Overloaded method to check if two timestamps are on the same day
    private boolean isSameDay(long timestamp1, long timestamp2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();

        cal1.setTimeInMillis(timestamp1);
        cal2.setTimeInMillis(timestamp2);

        return isSameDay(cal1, cal2);
    }

    // Format time as HH:mm
    private String formatTimestamp(Long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = chatMessages.get(position);
        return message.getSenderId().equals(currentUserId) ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    // ViewHolders
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, messageTime, messageDate;

        SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.sent_message_text);
            messageTime = itemView.findViewById(R.id.sent_message_time);
            messageDate = itemView.findViewById(R.id.sent_message_date);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, messageTime, messageDate;

        ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.received_message_text);
            messageTime = itemView.findViewById(R.id.received_message_time);
            messageDate = itemView.findViewById(R.id.received_message_date);
        }
    }
}