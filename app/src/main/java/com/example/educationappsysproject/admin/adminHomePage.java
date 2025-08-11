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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.educationappsysproject.Authentication.login;
import com.example.educationappsysproject.R;
import com.example.educationappsysproject.admin.addcourse_folder.addCourseName;
import com.example.educationappsysproject.admin.course.courseAdapter;
import com.example.educationappsysproject.homepage.chat.UserActivity;
import com.example.educationappsysproject.homepage.courseDetails;
import com.example.educationappsysproject.homepage.userDeatils;
import com.example.educationappsysproject.splashScreen;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class adminHomePage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public TextView userEmail, userName, userHomeName;
    public Button addCourse, viewStatsBtn, viewChatsBtn;
    FirebaseAuth auth;

    // For listing courses
    ListView courseListView;
    List<String> courseTitles = new ArrayList<>();
    List<String> documentIds = new ArrayList<>();
    
    // For statistics
    TextView totalCoursesText, totalUsersText, totalEnrollmentsText;

    // For drawer layout
    private DrawerLayout drawerLayout;
    Toolbar toolbar;

    FirebaseUser firebaseUser;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home_page);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firebaseUser = auth.getCurrentUser();

        // Initialize UI elements
        addCourse = findViewById(R.id.addVideoButton);
        viewStatsBtn = findViewById(R.id.viewStatsButton);
        viewChatsBtn = findViewById(R.id.viewChatsButton);
        userHomeName = findViewById(R.id.userHomeName);
        courseListView = findViewById(R.id.videoListView);
        totalCoursesText = findViewById(R.id.totalCoursesText);
        totalUsersText = findViewById(R.id.totalUsersText);
        totalEnrollmentsText = findViewById(R.id.totalEnrollmentsText);

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

        // Load statistics
        loadStatistics();
        
        // Add click listener for total users
        totalUsersText.setOnClickListener(v -> {
            showUserDetails();
        });

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
                    documentIds.clear();
                    for (DocumentSnapshot document : snapshots.getDocuments()) {
                        String courseTitle = document.getString("title");
                        if (courseTitle != null) {
                            courseTitles.add(courseTitle);
                            documentIds.add(document.getId());
                        }
                    }

                    String courseId = documentIds.isEmpty() ? "" : documentIds.get(0);
                    courseAdapter adapter = new courseAdapter(adminHomePage.this, courseTitles);
                    courseListView.setAdapter(adapter);
                    
                    // Update total courses
                    totalCoursesText.setText(String.valueOf(courseTitles.size()));
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

        // View Statistics Button
        viewStatsBtn.setOnClickListener(v -> {
            showDetailedStatistics();
        });

        // View Chats Button
        viewChatsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(adminHomePage.this, AdminChatActivity.class);
            startActivity(intent);
        });
    }

    private void loadStatistics() {
        // Load total users
        firestore.collection("users")
                .whereNotEqualTo("studentId", null)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    totalUsersText.setText(String.valueOf(queryDocumentSnapshots.size()));
                });

        // Load total enrollments
        firestore.collection("users")
                .get()
                .addOnSuccessListener(userSnapshots -> {
                    final int[] totalEnrollments = {0};
                    final int[] processedUsers = {0};
                    int totalUsers = userSnapshots.size();
                    
                    if (totalUsers == 0) {
                        totalEnrollmentsText.setText("0");
                        return;
                    }
                    
                    for (DocumentSnapshot userDoc : userSnapshots) {
                        firestore.collection("users")
                                .document(userDoc.getId())
                                .collection("enrolled_courses")
                                .get()
                                .addOnSuccessListener(enrollmentSnapshots -> {
                                    totalEnrollments[0] += enrollmentSnapshots.size();
                                    processedUsers[0]++;
                                    
                                    if (processedUsers[0] == totalUsers) {
                                        totalEnrollmentsText.setText(String.valueOf(totalEnrollments[0]));
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    processedUsers[0]++;
                                    if (processedUsers[0] == totalUsers) {
                                        totalEnrollmentsText.setText(String.valueOf(totalEnrollments[0]));
                                    }
                                });
                    }
                });
    }

    private void showDetailedStatistics() {
        firestore.collection("course")
                .get()
                .addOnSuccessListener(courseSnapshots -> {
                    Map<String, Integer> courseEnrollments = new HashMap<>();
                    
                    for (DocumentSnapshot courseDoc : courseSnapshots) {
                        String courseName = courseDoc.getString("title");
                        courseEnrollments.put(courseName, 0);
                    }

                    firestore.collection("users")
                            .get()
                            .addOnSuccessListener(userSnapshots -> {
                                final int[] processedUsers = {0};
                                final int totalUsers = userSnapshots.size();
                                
                                if (totalUsers == 0) {
                                    showStatisticsDialog(courseEnrollments);
                                    return;
                                }
                                
                                for (DocumentSnapshot userDoc : userSnapshots) {
                                    firestore.collection("users")
                                            .document(userDoc.getId())
                                            .collection("enrolled_courses")
                                            .get()
                                            .addOnSuccessListener(enrollmentSnapshots -> {
                                                for (DocumentSnapshot enrollmentDoc : enrollmentSnapshots) {
                                                    String courseName = enrollmentDoc.getString("courseName");
                                                    if (courseName != null && courseEnrollments.containsKey(courseName)) {
                                                        courseEnrollments.put(courseName, courseEnrollments.get(courseName) + 1);
                                                    }
                                                }
                                                
                                                processedUsers[0]++;
                                                if (processedUsers[0] == totalUsers) {
                                                    showStatisticsDialog(courseEnrollments);
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                processedUsers[0]++;
                                                if (processedUsers[0] == totalUsers) {
                                                    showStatisticsDialog(courseEnrollments);
                                                }
                                            });
                                }
                            });
                });
    }

    private void showStatisticsDialog(Map<String, Integer> courseEnrollments) {
        StringBuilder stats = new StringBuilder("📊 **Course Enrollment Statistics**\n\n");
        
        if (courseEnrollments.isEmpty()) {
            stats.append("No course enrollment data available.");
        } else {
            stats.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
            
            for (Map.Entry<String, Integer> entry : courseEnrollments.entrySet()) {
                stats.append("📚 **").append(entry.getKey()).append("**\n");
                stats.append("   👥 ").append(entry.getValue()).append(" students enrolled\n\n");
            }
            
            // Calculate total enrollments
            int totalEnrollments = courseEnrollments.values().stream().mapToInt(Integer::intValue).sum();
            stats.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            stats.append("📈 **Total Enrollments: ").append(totalEnrollments).append("**\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("📊 Course Statistics")
                .setMessage(stats.toString())
                .setPositiveButton("OK", null)
                .setNegativeButton("Refresh", (dialog, which) -> showDetailedStatistics())
                .show();
    }
    
    private void showUserDetails() {
        // Show loading dialog
        AlertDialog loadingDialog = new AlertDialog.Builder(this)
                .setTitle("Loading User Details")
                .setMessage("Please wait while we fetch user information...")
                .setCancelable(false)
                .show();

        firestore.collection("users")
                .whereNotEqualTo("studentId", null)
                .get()
                .addOnSuccessListener(userSnapshots -> {
                    final int[] processedUsers = {0};
                    final int totalUsers = userSnapshots.size();
                    final StringBuilder userDetails = new StringBuilder();
                    
                    if (totalUsers == 0) {
                        loadingDialog.dismiss();
                        new AlertDialog.Builder(this)
                                .setTitle("User Details")
                                .setMessage("No users found in the system.")
                                .setPositiveButton("OK", null)
                                .show();
                        return;
                    }
                    
                    userDetails.append("📊 Total Users: ").append(totalUsers).append("\n\n");
                    userDetails.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
                    
                    for (DocumentSnapshot userDoc : userSnapshots) {
                        String userName = userDoc.getString("Name");
                        String userEmail = userDoc.getString("Email");
                        String studentId = userDoc.getString("studentId");
                        Boolean isAdmin = userDoc.getBoolean("checkLevel");
                        
                        userDetails.append("👤 **").append(userName != null ? userName : "Unknown").append("**\n");
                        userDetails.append("📧 ").append(userEmail != null ? userEmail : "No email").append("\n");
                        userDetails.append("🆔 ").append(studentId != null ? studentId : "No ID").append("\n");
                        if (isAdmin != null && isAdmin) {
                            userDetails.append("👑 **Admin User**\n");
                        }
                        
                        // Get enrolled courses for this user
                        firestore.collection("users")
                                .document(userDoc.getId())
                                .collection("enrolled_courses")
                                .get()
                                .addOnSuccessListener(enrollmentSnapshots -> {
                                    if (!enrollmentSnapshots.isEmpty()) {
                                        userDetails.append("📚 **Enrolled Courses (").append(enrollmentSnapshots.size()).append("):**\n");
                                        for (DocumentSnapshot enrollmentDoc : enrollmentSnapshots) {
                                            String courseName = enrollmentDoc.getString("courseName");
                                            if (courseName != null) {
                                                userDetails.append("   📖 ").append(courseName).append("\n");
                                            }
                                        }
                                    } else {
                                        userDetails.append("📚 **No courses enrolled**\n");
                                    }
                                    userDetails.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
                                    
                                    processedUsers[0]++;
                                    if (processedUsers[0] == totalUsers) {
                                        loadingDialog.dismiss();
                                        showUserDetailsDialog(userDetails.toString());
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    userDetails.append("📚 **Error loading courses**\n");
                                    userDetails.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
                                    processedUsers[0]++;
                                    if (processedUsers[0] == totalUsers) {
                                        loadingDialog.dismiss();
                                        showUserDetailsDialog(userDetails.toString());
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Error loading user details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showUserDetailsDialog(String userDetails) {
        new AlertDialog.Builder(this)
                .setTitle("📊 User Details & Enrollments")
                .setMessage(userDetails.toString())
                .setPositiveButton("OK", null)
                .setNegativeButton("Refresh", (dialog, which) -> showUserDetails())
                .show();
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
