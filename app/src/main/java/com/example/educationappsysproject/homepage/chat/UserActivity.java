package com.example.educationappsysproject.homepage.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.educationappsysproject.Authentication.login;
import com.example.educationappsysproject.R;
import com.example.educationappsysproject.homepage.adapters.UserAdapter;
import com.example.educationappsysproject.homepage.allCoursesSection;
import com.example.educationappsysproject.homepage.chatBotActivity;
import com.example.educationappsysproject.homepage.homeScreen;
import com.example.educationappsysproject.homepage.models.Users;
import com.example.educationappsysproject.homepage.userDeatils;
import com.example.educationappsysproject.splashScreen;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private TextView userEmail, userName,userHomeName;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<Users> usersList;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Initialize Firebase
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // bottom navigation
        // Initialize BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set the default selected item (Home)
        bottomNavigationView.setSelectedItemId(R.id.nav_chatbot);

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


        // side navigation bar
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



        // Setup RecyclerView
        recyclerView = findViewById(R.id.users_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        usersList = new ArrayList<>();
        userAdapter = new UserAdapter(this, usersList);
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

        // Get current user's student ID
        fStore.collection("users")
                .document(fAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String currentUserStudentId = documentSnapshot.getString("studentId");

                    // Fetch all users with studentId
                    fStore.collection("users")
                            .whereNotEqualTo("studentId", null)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                usersList.clear();
                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    // Exclude current user
                                    if (!document.getId().equals(fAuth.getCurrentUser().getUid())) {
                                        Users user = new Users(
                                                document.getId(),
                                                document.getString("Name"),
                                                document.getString("Email"),
                                                document.getString("studentId")
                                        );
                                        usersList.add(user);
                                    }
                                }
                                userAdapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error fetching users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching current user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            fAuth.signOut();
            startActivity(new Intent(getApplicationContext(), login.class));
            finish();
            Toast.makeText(getApplicationContext(), "Log Out successfully", Toast.LENGTH_SHORT).show();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
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
}