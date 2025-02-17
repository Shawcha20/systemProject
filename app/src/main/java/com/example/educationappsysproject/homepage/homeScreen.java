package com.example.educationappsysproject.homepage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.educationappsysproject.homepage.chat.ChatActivity;
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

public class homeScreen extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private TextView userEmail, userName, userHomeName;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        userHomeName = findViewById(R.id.userHomeName);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        // bottom navigation
        // Initialize BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set the default selected item (Home)
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

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






        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        View headerView = navigationView.getHeaderView(0);
        userEmail = headerView.findViewById(R.id.drawerUserMail);
        userName = headerView.findViewById(R.id.drawerNameUser);

        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            fetchUserData(userId);
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }

        RecyclerView recyclerView = findViewById(R.id.coursesRecyclerView);
       // RecyclerView recyclerView1= findViewById(R.id.coursesRecyclerViewAll);
        List<Course> courseList = new ArrayList<>();
      //  List<Course>courseList1= new ArrayList<>();
        recyclerViewAdapter adapter = new recyclerViewAdapter(this, courseList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);


        fetchCourses(adapter, courseList);

        // fetch all courses
     //   fetchCourses(adapter,courseList1);
    }

    private void fetchUserData(String userId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference docRef = firestore.collection("users").document(userId);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(homeScreen.this, "Error loading user data", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    String name = snapshot.getString("Name");
                    String email = snapshot.getString("Email");

                    userName.setText(name);
                    userEmail.setText(email);
                    userHomeName.setText("Welcome " + name);
                } else {
                    Toast.makeText(homeScreen.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchCourses(recyclerViewAdapter adapter, List<Course> courseList) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference coursesRef = firestore.collection("course");

        coursesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                   Integer popular = document.get("popular", Integer.class);
                    if(popular > 1){
                    String title = document.getString("title");
                    String imageUrl = document.getString("imageUrl");

                    if (title != null && imageUrl != null) {

                        courseList.add(new Course(title, imageUrl));
                    } else {
                        Log.w("fetchCourses", "Missing title or imageUrl in course document");
                    }
                    }
                }
                adapter.notifyDataSetChanged();
            } else {
               Toast.makeText(homeScreen.this, "Error fetching courses", Toast.LENGTH_SHORT).show();
           }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int getUid = item.getItemId();

        if (getUid == R.id.nav_home) {
            startActivity(new Intent(homeScreen.this, homeScreen.class));
        } else if (getUid == R.id.nav_user) {
            startActivity(new Intent(homeScreen.this, userDeatils.class));
        } else if (getUid == R.id.nav_creator) {
            startActivity(new Intent(homeScreen.this, splashScreen.class));
        } else if (getUid == R.id.nav_logOutDrawer) {
            auth.signOut();
            startActivity(new Intent(homeScreen.this, login.class));
            finish();
            Toast.makeText(homeScreen.this, "Log Out successfully", Toast.LENGTH_SHORT).show();
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
