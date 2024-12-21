package com.example.educationappsysproject.admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.educationappsysproject.Authentication.login;
import com.example.educationappsysproject.R;
import com.example.educationappsysproject.admin.addcourse_folder.addCourseName;
import com.example.educationappsysproject.homepage.courseDetails;
import com.example.educationappsysproject.homepage.userDeatils;
import com.example.educationappsysproject.splashScreen;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class adminHomePage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    public TextView userEmail, userName ,  userHomeName ;
    public CardView admission , hsc ,ssc ;
    public Button addCourse;
    FirebaseDatabase database;
    DatabaseReference reference;

    FirebaseAuth auth;

    // for listing courses
    ListView courseListView;
    String[] courses={"course1 ", "course2", "course3"};
    // for drawer layout
    private DrawerLayout drawerLayout;
    Toolbar toolbar;

    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home_page);

        // adding course button
        addCourse=findViewById(R.id.addCourseButton);
        userHomeName = findViewById(R.id.userHomeName);


        // courselist
        courseListView=findViewById(R.id.courseListView);
        ArrayAdapter<String>adapter=new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,courses);
        courseListView.setAdapter(adapter);

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
                        Toast.makeText(com.example.educationappsysproject.admin.adminHomePage.this, "Error loading user data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        String name = snapshot.getString("Name");
                        String email = snapshot.getString("Email");

                        userName.setText(name);
                        userEmail.setText(email);
                        userHomeName.setText("Welcome " + name);
                    } else {
                        Toast.makeText(com.example.educationappsysproject.admin.adminHomePage.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        // going to add course activity
        addCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(adminHomePage.this, addCourseName.class);
                startActivity(intent);
            }
        });



        // forlist view
        DocumentReference CourseRef= firestore.collection("courses").document();
        CourseRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

            }
        });
        courseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedCourse = courses[position];
                Intent intent = new Intent(adminHomePage.this, courseDetails.class);
                intent.putExtra("courseName", selectedCourse);
                startActivity(intent);
            }
        });
    }


    //for drawer


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int getUid=item.getItemId();

        if(getUid == R.id.nav_home)
        {

            Intent i = new Intent(com.example.educationappsysproject.admin.adminHomePage.this, com.example.educationappsysproject.admin.adminHomePage.class);
            startActivity(i);
        }
        else if(getUid == R.id.nav_user){

            Intent i = new Intent(com.example.educationappsysproject.admin.adminHomePage.this, userDeatils.class);
            startActivity(i);


        }else if(getUid == R.id.nav_creator){

            Intent i = new Intent(com.example.educationappsysproject.admin.adminHomePage.this, splashScreen.class);
            startActivity(i);


        }
        else if(getUid == R.id.nav_logOutDrawer)
        {

            auth.signOut();
            Intent i = new Intent(com.example.educationappsysproject.admin.adminHomePage.this, login.class);
            startActivity(i);
            finish();
            Toast.makeText(com.example.educationappsysproject.admin.adminHomePage.this, "Log Out successfully", Toast.LENGTH_SHORT).show();
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
                    dialogInterface.dismiss(); // Close the dialog without exiting the app
                }
            });

            // Show the dialog
            builder.create().show();
        }
    }

    // i guess everything will work now

}
