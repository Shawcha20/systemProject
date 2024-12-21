package com.example.educationappsysproject.Authentication;

import static android.content.ContentValues.TAG;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.educationappsysproject.R;
import com.example.educationappsysproject.admin.adminHomePage;
import com.example.educationappsysproject.homepage.homeScreen;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class login extends AppCompatActivity {

    TextView goSignUp, forgotPass;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    EditText email, password;
    Button signIn;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance(); // Initialize Firestore here
        progressBar = findViewById(R.id.progressSignIn);
        email = findViewById(R.id.signInEmail);
        password = findViewById(R.id.signInPassword);
        signIn = findViewById(R.id.signIn);
        goSignUp = findViewById(R.id.toSignUP);
        forgotPass = findViewById(R.id.forgotPassword);

        // Check if user is already logged in
        FirebaseUser currentUser = fAuth.getCurrentUser();
        if (currentUser != null) {
            // User is logged in, check their access level
            checkUserAccessLevel(currentUser.getUid());
            return; // Avoid further execution of onCreate
        }

        // Sign-in button click listener
        signIn.setOnClickListener(v -> {
            String Remail = email.getText().toString().trim();
            String Rpassword = password.getText().toString().trim();

            if (TextUtils.isEmpty(Remail)) {
                Toast.makeText(login.this, "Email is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(Rpassword)) {
                Toast.makeText(login.this, "Password is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (Rpassword.length() < 6) {
                Toast.makeText(login.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            // Firebase Authentication
            fAuth.signInWithEmailAndPassword(Remail, Rpassword)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            FirebaseUser user = fAuth.getCurrentUser();
                            if (user != null) {
                                checkUserAccessLevel(user.getUid());
                            }
                        } else {
                            Toast.makeText(login.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Go to Sign Up screen
        goSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(login.this, signup.class);
            startActivity(intent);
            finish();
        });

        // Forgot Password
        forgotPass.setOnClickListener(v -> {
            EditText resetMail = new EditText(v.getContext());
            AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
            passwordResetDialog.setTitle("Reset Password");
            passwordResetDialog.setMessage("Enter your email to receive a reset link");
            passwordResetDialog.setView(resetMail);
            passwordResetDialog.setPositiveButton("Yes", (dialog, which) -> {
                String mail = resetMail.getText().toString();
                fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(unused ->
                        Toast.makeText(login.this, "Reset link sent to your email", Toast.LENGTH_SHORT).show()
                ).addOnFailureListener(e ->
                        Toast.makeText(login.this, "Error! Reset link not sent: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            });
            passwordResetDialog.setNegativeButton("No", (dialog, which) -> {
                // Dismiss dialog
            });
            passwordResetDialog.create().show();
        });
    }

    private void checkUserAccessLevel(String uid) {
        // Fetch user document from Firestore
        DocumentReference df = fStore.collection("users").document(uid);
        df.get()
                .addOnSuccessListener(documentSnapshot -> {
                    Log.d(TAG, "onSuccess: " + documentSnapshot.getData());
                    Boolean checkLevel = documentSnapshot.getBoolean("checkLevel");

                    // Handle access level
                    if (checkLevel != null && checkLevel) {
                        // Navigate to admin home page
                        Toast.makeText(login.this, "Admin logged in", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), adminHomePage.class));
                    } else {
                        // Navigate to regular home screen
                        Toast.makeText(login.this, "User logged in", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), homeScreen.class));
                    }
                    finish(); // Ensure only one navigation occurs
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching document: " + e.getMessage());
                    Toast.makeText(login.this, "Failed to verify access level. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }
}
