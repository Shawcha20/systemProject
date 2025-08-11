package com.example.educationappsysproject.homepage.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.educationappsysproject.R;
import com.example.educationappsysproject.homepage.adapters.ChatAdapter;
import com.example.educationappsysproject.homepage.allCoursesSection;
import com.example.educationappsysproject.homepage.chatBotActivity;
import com.example.educationappsysproject.homepage.homeScreen;
import com.example.educationappsysproject.homepage.models.ChatMessage;
import com.example.educationappsysproject.homepage.searchCourseActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton;

    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;

    private String receiverId;
    private String receiverName;
    private String currentUserId;

    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize Firebase
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        currentUserId = fAuth.getCurrentUser().getUid();

        // Get Intent Extras
        receiverId = getIntent().getStringExtra("userId");
        receiverName = getIntent().getStringExtra("userName");

        // Initialize Views
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);

        // Setup RecyclerView
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, chatMessages, currentUserId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);

        // Load Messages
        loadMessages();

        // Send Message
        sendButton.setOnClickListener(v -> sendMessage());

        // Bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_chatbot);

        // Handle bottom navigation clicks
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), homeScreen.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_chat) {
                startActivity(new Intent(getApplicationContext(), allCoursesSection.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_chatbot) {
                startActivity(new Intent(getApplicationContext(), UserActivity.class));
                overridePendingTransition(0, 0);
//                return true;
            } else if (itemId == R.id.nav_course) {
                startActivity(new Intent(getApplicationContext(), chatBotActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            else if(itemId==R.id.nav_search){
                startActivity(new Intent(getApplicationContext(), searchCourseActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });


    }

    private void loadMessages() {
        String chatRoomId = generateChatRoomId(currentUserId, receiverId);

        fStore.collection("chats")
                .document(chatRoomId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading messages: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    chatMessages.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot document : value) {
                            ChatMessage message = new ChatMessage(
                                    document.getString("senderId"),
                                    document.getString("receiverId"),
                                    document.getString("message"),
                                    document.getLong("timestamp")
                            );
                            chatMessages.add(message);
                        }

                        chatAdapter.notifyDataSetChanged();

                        if (!chatMessages.isEmpty()) {
                            chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                        }
                    }
                });
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();

        if (TextUtils.isEmpty(messageText)) {
            return;
        }

        String chatRoomId = generateChatRoomId(currentUserId, receiverId);

        Map<String, Object> message = new HashMap<>();
        message.put("senderId", currentUserId);
        message.put("receiverId", receiverId);
        message.put("message", messageText);
        message.put("timestamp", System.currentTimeMillis());
        message.put("type", "text");

        fStore.collection("chats")
                .document(chatRoomId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    messageInput.setText("");
                    updateUserList(currentUserId, receiverId, messageText);

                    if (!chatMessages.isEmpty()) {
                        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserList(String senderId, String receiverId, String messageText) {
        fStore.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        if (document.getId().equals(senderId) || document.getId().equals(receiverId)) {
                            Map<String, Object> userUpdate = new HashMap<>();
                            userUpdate.put("lastMessage", messageText);
                            userUpdate.put("lastMessageTimestamp", System.currentTimeMillis());

                            fStore.collection("users").document(document.getId()).update(userUpdate);
                        }
                    }
                });
    }


    private String generateChatRoomId(String user1, String user2) {
        return user1.compareTo(user2) < 0 ? user1 + "" + user2 : user2 + "" + user1;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
}
}
