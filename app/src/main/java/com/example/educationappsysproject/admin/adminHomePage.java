package com.example.educationappsysproject.admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
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

import com.example.educationappsysproject.Authentication.login;
import com.example.educationappsysproject.R;
import com.example.educationappsysproject.admin.addcourse_folder.addCourseName;
import com.example.educationappsysproject.admin.course.courseAdapter;
import com.example.educationappsysproject.homepage.courseDetails;
import com.example.educationappsysproject.homepage.userDeatils;
import com.example.educationappsysproject.splashScreen;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class adminHomePage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public TextView userEmail, userName, userHomeName;
    public Button addCourse;
    FirebaseAuth auth;

    // For listing courses
    ListView courseListView;
    List<String> courseTitles = new ArrayList<>();
    List<String> documentIds = new ArrayList<>();

    // For drawer layout
    private DrawerLayout drawerLayout;
    Toolbar toolbar;

    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home_page);

        // Initialize UI elements
        addCourse = findViewById(R.id.addVideoButton);
        userHomeName = findViewById(R.id.userHomeName);
        courseListView = findViewById(R.id.videoListView);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        // For Drawer
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        View headerView = navigationView.getHeaderView(0);
        userEmail = headerView.findViewById(R.id.drawerUserMail);
        userName = headerView.findViewById(R.id.drawerNameUser);

        // Fetch user data from Firestore
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();

            firestore.collection("users").document(userId)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                            if (error != null) {
                                Toast.makeText(adminHomePage.this, "Error loading user data", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (snapshot != null && snapshot.exists()) {
                                String name = snapshot.getString("Name");
                                String email = snapshot.getString("Email");

                                userName.setText(name);
                                userEmail.setText(email);
                                userHomeName.setText("Welcome " + name);
                            } else {
                                Toast.makeText(adminHomePage.this, "User data not found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

        // Fetch courses from Firestore
        // Fetch courses from Firestore
        firestore.collection("course").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(adminHomePage.this, "Error fetching courses: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (snapshots != null) {
                    courseTitles.clear();
                    documentIds.clear(); // Clear the existing document IDs
                    for (DocumentSnapshot document : snapshots.getDocuments()) {
                        String courseTitle = document.getString("title"); // Assuming "title" holds the course name
                        if (courseTitle != null) {
                            courseTitles.add(courseTitle);
                            documentIds.add(document.getId()); // Save the document ID
                        }
                    }

                    // Pass the first document ID or another default value as the courseId
                    String courseId = documentIds.isEmpty() ? "" : documentIds.get(0);
                    courseAdapter adapter = new courseAdapter(adminHomePage.this, courseTitles);
                    courseListView.setAdapter(adapter);
                }
            }
        });


        // Handle ListView item click
        courseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedCourse = courseTitles.get(position);
                String documentId = documentIds.get(position);
                Toast.makeText(adminHomePage.this, "Selected: " + selectedCourse, Toast.LENGTH_SHORT).show();
                // Log the values
                System.out.println("Selected Course: " + selectedCourse + ", Document ID: " + documentId);

                Intent intent = new Intent(adminHomePage.this, courseDetails.class);
                intent.putExtra("courseName", selectedCourse);
                intent.putExtra("documentId", documentId);
                startActivity(intent);
            }
        });


        // Add Course Button
        addCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(adminHomePage.this, addCourseName.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int getUid = item.getItemId();

        if (getUid == R.id.nav_home) {
            Intent i = new Intent(adminHomePage.this, adminHomePage.class);
            startActivity(i);
        } else if (getUid == R.id.nav_user) {
            Intent i = new Intent(adminHomePage.this, userDeatils.class);
            startActivity(i);
        } else if (getUid == R.id.nav_creator) {
            Intent i = new Intent(adminHomePage.this, splashScreen.class);
            startActivity(i);
        } else if (getUid == R.id.nav_logOutDrawer) {
            auth.signOut();
            Intent i = new Intent(adminHomePage.this, login.class);
            startActivity(i);
            finish();
            Toast.makeText(adminHomePage.this, "Log Out successfully", Toast.LENGTH_SHORT).show();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(adminHomePage.this);
            builder.setMessage("Are you sure you want to QUIT the app?");
            builder.setCancelable(false);

            builder.setNegativeButton("QUIT APP", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    moveTaskToBack(true);
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                }
            });

            builder.setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            builder.create().show();
        }
    }
}
