package com.example.educationappsysproject.homepage.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.educationappsysproject.R;
import com.example.educationappsysproject.homepage.adapters.ChatAdapter;
import com.example.educationappsysproject.homepage.allCoursesSection;
import com.example.educationappsysproject.homepage.chatBotActivity;
import com.example.educationappsysproject.homepage.homeScreen;
import com.example.educationappsysproject.homepage.models.ChatMessage;
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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        // Initialize Firebase
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        currentUserId = fAuth.getCurrentUser().getUid();


        // bottom navigation
        // Initialize BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set the default selected item (Home)
        bottomNavigationView.setSelectedItemId(R.id.nav_chat);

        // Handle bottom navigation clicks
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(getApplicationContext(), homeScreen.class)); // Already in home screen
                } else if (itemId == R.id.nav_chat) {
                    startActivity(new Intent(getApplicationContext(), allCoursesSection.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_chatbot) {
                    startActivity(new Intent(getApplicationContext(), UserActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_course) {
                    startActivity(new Intent(getApplicationContext(), chatBotActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            }
        });



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
    }

    private void loadMessages() {
        // Create a unique chat room ID (sorted to ensure consistency)
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

                        // Scroll to the last message
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

        // Create a unique chat room ID (sorted to ensure consistency)
        String chatRoomId = generateChatRoomId(currentUserId, receiverId);

        // Prepare message data
        Map<String, Object> message = new HashMap<>();
        message.put("senderId", currentUserId);
        message.put("receiverId", receiverId);
        message.put("message", messageText);
        message.put("timestamp", System.currentTimeMillis());
        message.put("type", "text"); // Add message type

        // Save to Firestore
        fStore.collection("chats")
                .document(chatRoomId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    // Clear input after sending
                    messageInput.setText("");

                    // Scroll to bottom
                    if (!chatMessages.isEmpty()) {
                        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Generate a consistent chat room ID
    private String generateChatRoomId(String user1, String user2) {
        // Ensure the chat room ID is the same regardless of who starts the chat
        return user1.compareTo(user2) < 0 ? user1 + "" + user2 : user2 + "" + user1;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
