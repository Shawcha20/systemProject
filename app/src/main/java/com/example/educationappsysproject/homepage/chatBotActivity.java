package com.example.educationappsysproject.homepage;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.educationappsysproject.BuildConfig;
import com.example.educationappsysproject.R;
import com.example.educationappsysproject.homepage.adapters.MessageAdapter;
import com.example.educationappsysproject.homepage.chat.UserActivity;
import com.example.educationappsysproject.homepage.models.Message;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class chatBotActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText userInput;
    private ImageButton sendButton;

    private MessageAdapter adapter;
    private final List<Message> messages = new ArrayList<>();

    private String apiKey; // Loaded securely

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        userInput = findViewById(R.id.userInput);
        sendButton = findViewById(R.id.sendButton);

        adapter = new MessageAdapter(messages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(adapter);
        //bottom navigation
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle("StudyNest Chatbot");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // bottom navigation
        // Initialize BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set the default selected item (Home)
        bottomNavigationView.setSelectedItemId(R.id.nav_course);

        // Handle bottom navigation clicks
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(getApplicationContext(), homeScreen.class)); // Already in home screen
                    overridePendingTransition(0, 0);
                } else if (itemId == R.id.nav_chat) {
                    startActivity(new Intent(getApplicationContext(), allCoursesSection.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_chatbot) {
                    startActivity(new Intent(getApplicationContext(), UserActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_course) {
                    startActivity(new Intent(getApplicationContext(),chatBotActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                else if(itemId==R.id.nav_search){
                    startActivity(new Intent(getApplicationContext(), searchCourseActivity.class));
                    overridePendingTransition(0, 0);
                    return true;                }
                return false;
            }
        });
        // Securely load API key
        apiKey = BuildConfig.GEMINI_API_KEY;

        sendButton.setOnClickListener(v -> {
            String message = userInput.getText().toString().trim();
            if (!message.isEmpty()) {
                addMessage(message, true);
                userInput.setText("");
                sendToGeminiAPI(message);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addMessage(String content, boolean isUser ) {
        messages.add(new Message(content, isUser ));
        runOnUiThread(() -> {
            adapter.notifyItemInserted(messages.size() - 1);
            chatRecyclerView.scrollToPosition(messages.size() - 1);
        });
    }

    private void sendToGeminiAPI(String prompt) {
        new Thread(() -> {
            try {
                URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                // ===== context about your project (permanent) ====
                String systemContext =
                        "StudyNest is the name of this app"+
                                "This course offers all modern educational support "+
                        "You are an AI chatbot for an Android Education App named StudyNest. " +
                                "This app includes login/sign-up, courses (Java, Python, C, C++,C#, Javascript, HTML,PHP), " +
                                "and book recommendations for each course. " +
                                "When user asks about a course, you must answer helpfully and friendly.";
                // =================================================

                // Build correct JSON body: role must be 'user', parts must be under that
                JSONObject requestJson = new JSONObject();
                JSONArray contentsArray = new JSONArray();

                JSONObject userContent = new JSONObject();
                userContent.put("role", "user");

                // combine context + actual user message:
                String finalPrompt = systemContext + "\n\nUser: " + prompt;

                JSONArray partsArray = new JSONArray();
                JSONObject textPart = new JSONObject();
                textPart.put("text", finalPrompt);
                partsArray.put(textPart);

                userContent.put("parts", partsArray);
                contentsArray.put(userContent);

                requestJson.put("contents", contentsArray);

                // send
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(requestJson.toString().getBytes("UTF-8"));
                }

                int responseCode = conn.getResponseCode();
                Scanner scanner;
                if (responseCode == 200) {
                    scanner = new Scanner(conn.getInputStream());
                } else {
                    scanner = new Scanner(conn.getErrorStream());
                }

                StringBuilder responseBuilder = new StringBuilder();
                while (scanner.hasNextLine()) {
                    responseBuilder.append(scanner.nextLine());
                }
                scanner.close();

                if (responseCode != 200) {
                    addMessage("Bot error: HTTP " + responseCode + "\n" + responseBuilder.toString(), false);
                    return;
                }

                // parse output
                JSONObject jsonResponse = new JSONObject(responseBuilder.toString());
                JSONArray candidates = jsonResponse.getJSONArray("candidates");
                JSONObject firstCandidate = candidates.getJSONObject(0);
                JSONArray partsArr = firstCandidate.getJSONObject("content").getJSONArray("parts");
                String reply = partsArr.getJSONObject(0).getString("text");

                addMessage(reply, false); // add bot reply

            } catch (Exception e) {
                addMessage("Bot error: " + e.getMessage(), false);
            }
        }).start();
}

}
