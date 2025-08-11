package com.example.educationappsysproject.Authentication;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.educationappsysproject.R;
import com.example.educationappsysproject.homepage.homeScreen;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class signup extends AppCompatActivity {

    TextView goToSignIn;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    TextInputEditText email, sid , password ,sName;
    Button register;
    Drawable drawable;
    FirebaseUser firebaseUser;
    FirebaseFirestore fStore;
    String userId;
    Handler handler;
    Runnable verificationCheckRunnable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        goToSignIn =findViewById(R.id.toSignIn);
        progressBar=findViewById(R.id.progressSignUp);
        email=findViewById(R.id.signUpEmail);
        sid=findViewById(R.id.studentId);
        password=findViewById(R.id.signUpPassword);
        sName=findViewById(R.id.studentName);
        fAuth= FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();
        register=findViewById(R.id.signUpBtn);


        // email verification
        handler = new Handler(Looper.getMainLooper());
      //  db=FirebaseDatabase.getInstance();
        if(fAuth.getCurrentUser() !=null)
        {
            startActivity(new Intent(getApplicationContext(), homeScreen.class));
            finish();
        }
        register.setOnClickListener(v-> {
            String Remail = email.getText().toString().trim();
            String Rpassword = password.getText().toString().trim();
            String Rsid= sid.getText().toString().trim();
            String RsName= sName.getText().toString().trim();
            if (TextUtils.isEmpty(Remail)) {

                Toast.makeText(signup.this, "Email is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if(TextUtils.isEmpty(Rsid))
            {
                Toast.makeText(signup.this, "student id must be filled", Toast.LENGTH_SHORT).show();
                return;
            }
            if(TextUtils.isEmpty(RsName))
            {
                Toast.makeText(signup.this,"student name must be filled", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(Rpassword)) {
//                Snackbar.make(findViewById(android.R.id.content), "Password field is required", Snackbar.LENGTH_SHORT).show();

//                Rpassword.setError("Password field is required");
                Toast.makeText(signup.this, "password filed is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (Rpassword.length() < 6)
            {
                Toast.makeText(signup.this, "Password must be at least 6 charecter", Toast.LENGTH_SHORT).show();
                return;
            }
            progressBar.setVisibility(View.VISIBLE);
            fAuth.createUserWithEmailAndPassword(Remail, Rpassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful())
                    {
                        firebaseUser= fAuth.getCurrentUser();
                        firebaseUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(signup.this, "verification email has been sent", Toast.LENGTH_SHORT).show();
                                startEmailVerificationCheck();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("tag", "onFailure: ");
                            }
                        });

                        Toast.makeText(signup.this,"Signed up", Toast.LENGTH_SHORT).show();
                        userId=fAuth.getCurrentUser().getUid();
                        DocumentReference documentReference=fStore.collection("users").document(userId);
                        Map<String,Object> user= new HashMap<>();
                        user.put("studentId",Rsid);
                        user.put("Email", Remail);
                        user.put("Name",RsName);
                        user.put("checkLevel",false);
                        user.put("profileImg","");
                        documentReference.set(user).addOnSuccessListener(unused -> {

                               //Toast.makeText(signup.this,"success"+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                               Intent intent= new Intent(getApplicationContext(), homeScreen.class);
                               startActivity(intent);
                                finish();

                        }).addOnFailureListener(e->{
                            Toast.makeText(signup.this, "firestore error"+e.getMessage() ,Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        });

                    }
                    else{
                        Toast.makeText(signup.this, "Error"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
        goToSignIn.setOnClickListener(v->{
            startActivity(new Intent(getApplicationContext(), login.class));
            finish();
        });
    }
    private void startEmailVerificationCheck() {
        verificationCheckRunnable = new Runnable() {
            @Override
            public void run() {
                firebaseUser = fAuth.getCurrentUser();
                if (firebaseUser != null) {
                    firebaseUser.reload(); // Reload the user data

                    if (firebaseUser.isEmailVerified()) {
                        Toast.makeText(signup.this, "Email verified! Redirecting to home screen.", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        Intent intent = new Intent(getApplicationContext(), homeScreen.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Retry after 3 seconds
                        handler.postDelayed(verificationCheckRunnable, 3000);
                    }
                }
            }
        };

        // Start the runnable to check email verification
        handler.post(verificationCheckRunnable);
    }

    // Stop checking for email verification if the user leaves the screen
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && verificationCheckRunnable != null) {
            handler.removeCallbacks(verificationCheckRunnable);
        }
    }
}

