package com.example.educationappsysproject.homepage;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.educationappsysproject.Authentication.login;
import com.example.educationappsysproject.Authentication.signup;
import com.example.educationappsysproject.R;
import com.example.educationappsysproject.splashScreen;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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


    public TextView userEmail, userName ,  userHomeName ;
    public CardView admission , hsc ,ssc ;

    FirebaseDatabase database;
    DatabaseReference reference;

    FirebaseAuth auth;

    // for drawer layout
    private DrawerLayout drawerLayout;
    Toolbar toolbar;

    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        admission = findViewById(R.id.admissionCard);
        hsc = findViewById(R.id.hscCard);
        ssc = findViewById(R.id.sscCard);
        userHomeName = findViewById(R.id.userHomeName);

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

        // Card click listeners
        admission.setOnClickListener(v -> startActivity(new Intent(homeScreen.this, courseDetails.class)));
        hsc.setOnClickListener(v -> Toast.makeText(homeScreen.this, "Upcoming HSC section", Toast.LENGTH_SHORT).show());
        ssc.setOnClickListener(v -> Toast.makeText(homeScreen.this, "Upcoming SSC section", Toast.LENGTH_SHORT).show());
    }

    //for drawer
//
//    private void fetchCourses() {
//        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
//        CollectionReference coursesRef = firestore.collection("courses");
//
//        coursesRef.get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                List<String> courseNames = new ArrayList<>();
//                for (QueryDocumentSnapshot document : task.getResult()) {
//                    String courseName = document.getString("CourseName");
//                    courseNames.add(courseName);
//                }
//
//                // Update the CardView text dynamically
//                if (courseNames.size() > 0) {
//                    admission.setText(courseNames.get(0)); // Set the first course to Admission card
//                }
//                if (courseNames.size() > 1) {
//                    hsc.setText(courseNames.get(1)); // Set the second course to HSC card
//                }
//                if (courseNames.size() > 2) {
//                    ssc.setText(courseNames.get(2)); // Set the third course to SSC card
//                }
//            } else {
//                Toast.makeText(homeScreen.this, "Error fetching courses", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int getUid=item.getItemId();

        if(getUid == R.id.nav_home)
        {

            Intent i = new Intent(homeScreen.this,homeScreen.class);
            startActivity(i);
        }
        else if(getUid == R.id.nav_user){

            Intent i = new Intent(homeScreen.this, userDeatils.class);
            startActivity(i);


        }else if(getUid == R.id.nav_creator){

            Intent i = new Intent(homeScreen.this, splashScreen.class);
            startActivity(i);


        }
        else if(getUid == R.id.nav_logOutDrawer)
        {

            auth.signOut();
            Intent i = new Intent(homeScreen.this, login.class);
            startActivity(i);
            finish();
            Toast.makeText(homeScreen.this, "Log Out successfully", Toast.LENGTH_SHORT).show();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    //end drawer
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            // Close the navigation drawer if it's open
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // Show the AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(homeScreen.this);
            builder.setMessage("Are you sure you want to QUIT App?");
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
                    dialogInterface.dismiss(); // Close the dialog without exiting the app
                }
            });

            // Show the dialog
            builder.create().show();
        }
    }


}
