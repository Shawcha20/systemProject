package com.example.educationappsysproject.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.educationappsysproject.Authentication.login;
import com.example.educationappsysproject.R;
import com.example.educationappsysproject.homepage.adapters.UserAdapter;
import com.example.educationappsysproject.homepage.models.Users;
import com.example.educationappsysproject.splashScreen;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminChatActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private TextView userEmail, userName;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<Users> usersList;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private FirebaseUser firebaseUser;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chat);

        // Initialize Firebase
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Side navigation bar
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        View headerView = navigationView.getHeaderView(0);
        userEmail = headerView.findViewById(R.id.drawerUserMail);
        userName = headerView.findViewById(R.id.drawerNameUser);

        firebaseUser = fAuth.getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
            fetchUserData(currentUserId);
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }

        // Setup RecyclerView
        recyclerView = findViewById(R.id.users_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        usersList = new ArrayList<>();
        userAdapter = new UserAdapter(this, usersList, currentUserId);
        recyclerView.setAdapter(userAdapter);

        // Fetch Users
        fetchUsers();
    }

    private void fetchUsers() {
        // Ensure user is logged in
        if (fAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, login.class));
            finish();
            return;
        }

        // Fetch all users with studentId (normal users)
        fStore.collection("users")
                .whereNotEqualTo("studentId", null)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error fetching users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    usersList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Exclude current user
                        if (!document.getId().equals(fAuth.getCurrentUser().getUid())) {
                            String profileImageUrl = document.getString("profileImageUrl");
                            Boolean isAdmin = document.getBoolean("checkLevel");
                            
                            Users user = new Users(
                                    document.getId(),
                                    document.getString("Name"),
                                    document.getString("Email"),
                                    document.getString("studentId"),
                                    document.getString("lastMessage"),
                                    document.getLong("lastMessageTimestamp"),
                                    profileImageUrl,
                                    isAdmin
                            );
                            usersList.add(user);
                        }
                    }
                    
                    // Sort users based on last message timestamp, handling nulls
                    usersList.sort((u1, u2) -> {
                        Long timestamp1 = u1.getLastMessageTimestamp();
                        Long timestamp2 = u2.getLastMessageTimestamp();

                        // Handle null values
                        if (timestamp1 == null && timestamp2 == null) return 0;
                        if (timestamp1 == null) return 1;
                        if (timestamp2 == null) return -1;

                        return timestamp2.compareTo(timestamp1); // Sort in descending order
                    });
                    userAdapter.notifyDataSetChanged();
                });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int getUid = item.getItemId();

        if (getUid == R.id.nav_home) {
            startActivity(new Intent(getApplicationContext(), adminHomePage.class));
        } else if (getUid == R.id.nav_user) {
            startActivity(new Intent(getApplicationContext(), com.example.educationappsysproject.homepage.userDeatils.class));
        } else if (getUid == R.id.nav_creator) {
            startActivity(new Intent(getApplicationContext(), splashScreen.class));
        } else if (getUid == R.id.nav_logOutDrawer) {
            fAuth.signOut();
            startActivity(new Intent(getApplicationContext(), login.class));
            finish();
            Toast.makeText(getApplicationContext(), "Log Out successfully", Toast.LENGTH_SHORT).show();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void fetchUserData(String userId) {
        fStore.collection("users").document(userId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                        } else {
                            Toast.makeText(getApplicationContext(), "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
