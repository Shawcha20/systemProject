package com.example.educationappsysproject.homepage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.educationappsysproject.Authentication.login;
import com.example.educationappsysproject.R;
import com.example.educationappsysproject.homepage.adapters.recyclerViewAdapter;
import com.example.educationappsysproject.homepage.chat.UserActivity;
import com.example.educationappsysproject.splashScreen;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class searchCourseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private TextView userEmail, userName, userHomeName, resultsHeader, noResultsLayout;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private Button searchButton;
    private EditText searchEditText;
    private View noResultsView;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_course);
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        
        // Initialize views
        searchButton = findViewById(R.id.search_button);
        searchEditText = findViewById(R.id.search_edit_text);
        resultsHeader = findViewById(R.id.resultsHeader);
        noResultsView = findViewById(R.id.noResultsLayout);
        recyclerView = findViewById(R.id.coursesRecyclerViewSearchedCourses);
        
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_search);

        // Handle bottom navigation clicks
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(getApplicationContext(), homeScreen.class));
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
            }
        });

        // Setup RecyclerView
        List<homeScreen.Course> courseList = new ArrayList<>();
        recyclerViewAdapter adapter = new recyclerViewAdapter(this, courseList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // Search button click listener
        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                searchCourses(query, adapter, courseList);
            } else {
                Toast.makeText(searchCourseActivity.this, "Please enter course name", Toast.LENGTH_SHORT).show();
            }
        });

        // Search on Enter key press
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                String query = searchEditText.getText().toString().trim();
                if (!query.isEmpty()) {
                    searchCourses(query, adapter, courseList);
                } else {
                    Toast.makeText(searchCourseActivity.this, "Please enter course name", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });
    }

    private void fetchUserData(String userId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference docRef = firestore.collection("users").document(userId);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(getApplicationContext(), "Error loading user data", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    String name = snapshot.getString("Name");
                    String email = snapshot.getString("Email");
                    userName.setText(name);
                    userEmail.setText(email);
                    userHomeName.setText("Welcome " + name);
                } else {
                    Toast.makeText(getApplicationContext(), "User data not found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchCourses(recyclerViewAdapter adapter, List<homeScreen.Course> courseList) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference coursesRef = firestore.collection("course");

        coursesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String title = document.getString("title");
                    String imageUrl = document.getString("imageUrl");

                    if (title != null && imageUrl != null) {
                        courseList.add(new homeScreen.Course(title, imageUrl));
                    } else {
                        Log.w("fetchCourses", "Missing title or imageUrl in course document");
                    }
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getApplicationContext(), "Error fetching courses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchCourses(String query, recyclerViewAdapter adapter, List<homeScreen.Course> courseList) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference coursesRef = firestore.collection("course");

        // Show loading state
        resultsHeader.setVisibility(View.VISIBLE);
        noResultsView.setVisibility(View.GONE);

        coursesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                courseList.clear();  // clear previous results

                for (QueryDocumentSnapshot document : task.getResult()) {
                    String title = document.getString("title");
                    String imageUrl = document.getString("imageUrl");

                    if (title != null && imageUrl != null) {
                        if (title.toLowerCase().contains(query.toLowerCase())) {
                            courseList.add(new homeScreen.Course(title, imageUrl));
                        }
                    }
                }

                adapter.notifyDataSetChanged();

                // Update UI based on results
                if (courseList.isEmpty()) {
                    resultsHeader.setVisibility(View.GONE);
                    noResultsView.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "No courses found matching '" + query + "'", Toast.LENGTH_SHORT).show();
                } else {
                    resultsHeader.setVisibility(View.VISIBLE);
                    noResultsView.setVisibility(View.GONE);
                    resultsHeader.setText("Found " + courseList.size() + " course(s)");
                }
            } else {
                Toast.makeText(getApplicationContext(), "Failed to fetch courses", Toast.LENGTH_SHORT).show();
                resultsHeader.setVisibility(View.GONE);
                noResultsView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int getUid = item.getItemId();

        if (getUid == R.id.nav_home) {
            startActivity(new Intent(getApplicationContext(), homeScreen.class));
        } else if (getUid == R.id.nav_user) {
            startActivity(new Intent(getApplicationContext(), userDeatils.class));
        } else if (getUid == R.id.nav_creator) {
            startActivity(new Intent(getApplicationContext(), splashScreen.class));
        } else if (getUid == R.id.nav_logOutDrawer) {
            auth.signOut();
            startActivity(new Intent(getApplicationContext(), login.class));
            finish();
            Toast.makeText(getApplicationContext(), "Log Out successfully", Toast.LENGTH_SHORT).show();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            new AlertDialog.Builder(this)
                    .setMessage("Are you sure you want to exit?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, id) -> finishAffinity())
                    .setNegativeButton("No", (dialog, id) -> dialog.dismiss())
                    .show();
        }
    }

    public static class Course {
        private String title;
        private String imageResourceId;

        public Course(String title, String imageResourceId) {
            this.title = title;
            this.imageResourceId = imageResourceId;
        }

        public String getTitle() {
            return title;
        }

        public String getImageResourceId() {
            return imageResourceId;
        }
    }
}