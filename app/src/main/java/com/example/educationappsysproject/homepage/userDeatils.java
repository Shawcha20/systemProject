package com.example.educationappsysproject.homepage;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.educationappsysproject.R;
import com.example.educationappsysproject.homepage.adapters.BookAdapter;
import com.example.educationappsysproject.homepage.adapters.BookRecommendation;
import com.example.educationappsysproject.homepage.models.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class userDeatils extends AppCompatActivity {

    private TextView userEmail, userName, userRoll, welcome;
    private EditText editName, editRoll;
    private ImageView profileImage, editButton;
    private Button backToHomePage, bookRecommend, saveButton, enrolledCoursesBtn, viewResultsBtn;
    private LinearLayout editLayout, viewLayout, booksSection;
    private FirebaseUser firebaseUser;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private static final String TAG = "UsersActivity";
    private RecyclerView booksRecyclerView;
    private BookAdapter bookAdapter;
    private Spinner countSpinner;
    private List<Book> allBooks = new ArrayList<>();
    private List<Book> allRecommendedBooks = new ArrayList<>();
    private Set<String> courseNames = new HashSet<>();
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private String currentUserId;
    //news
    private StorageReference storageReference;
    private ProgressDialog progressDialog;

    private Uri selectedImageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_deatils);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Initialize views
        welcome = findViewById(R.id.welcome);
        userEmail = findViewById(R.id.fEmail);
        userName = findViewById(R.id.fName);
        userRoll = findViewById(R.id.fRoll);
        editName = findViewById(R.id.editName);
        editRoll = findViewById(R.id.editRoll);
        profileImage = findViewById(R.id.profileImage);   //added
        editButton = findViewById(R.id.editButton);
        backToHomePage = findViewById(R.id.backToHomePage);
        bookRecommend = findViewById(R.id.bookRecommendBtn);
        saveButton = findViewById(R.id.saveButton);
        enrolledCoursesBtn = findViewById(R.id.enrolledCoursesBtn);
        viewResultsBtn = findViewById(R.id.viewResultsBtn);
        editLayout = findViewById(R.id.editLayout);
        viewLayout = findViewById(R.id.viewLayout);
        booksSection = findViewById(R.id.booksSection);
        booksRecyclerView = findViewById(R.id.booksRecyclerView);
        countSpinner = findViewById(R.id.countSpinner);
        //image progress bar
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);

        // RecyclerView setup
        booksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookAdapter = new BookAdapter(new ArrayList<>());
        booksRecyclerView.setAdapter(bookAdapter);
        booksRecyclerView.setVisibility(View.GONE); // Hide initially

        // Spinner setup
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.book_counts, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countSpinner.setAdapter(adapter);
        countSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateDisplayedBooks();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Load data (books will be loaded when Books button is clicked)
        loadBooksFromJson();

        // Firebase auth
        firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUserId = firebaseUser.getUid();

        // Load user info from Firestore
        loadUserData();

        // Edit button click listener
        editButton.setOnClickListener(v -> {
            viewLayout.setVisibility(View.GONE);
            editLayout.setVisibility(View.VISIBLE);
            editName.setText(userName.getText());
            editRoll.setText(userRoll.getText());
        });

        // Save button click listener
        saveButton.setOnClickListener(v -> {
            String newName = editName.getText().toString().trim();
            String newRoll = editRoll.getText().toString().trim();

            if (newName.isEmpty() || newRoll.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update Firestore
            DocumentReference userRef = firestore.collection("users").document(currentUserId);
            userRef.update("Name", newName, "studentId", newRoll)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        viewLayout.setVisibility(View.VISIBLE);
                        editLayout.setVisibility(View.GONE);
                        loadUserData(); // Reload data
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    });
        });

        // Profile image click listener
        profileImage.setOnClickListener(v -> {
            if (checkPermission()) {
                openImagePicker();
            } else {
                requestPermission();
            }
        });

        // Enrolled courses button
        enrolledCoursesBtn.setOnClickListener(v -> {
            showEnrolledCourses();
        });

        // Back button
        backToHomePage.setOnClickListener(v ->
                startActivity(new Intent(userDeatils.this, homeScreen.class))
        );

        // Book recommendation button logic
        bookRecommend.setOnClickListener(v -> {
            // Load user data and recommended books when Books button is clicked
            getCurrentUserAndRecommendBooks();

            // Show loading message
            Toast.makeText(userDeatils.this, "Loading recommended books...", Toast.LENGTH_SHORT).show();
        });
    }

    // Load books from assets/output.json
    private void loadBooksFromJson() {
        try {
            AssetManager assetManager = getAssets();
            InputStream inputStream = assetManager.open("output.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();

            String json = new String(buffer, "UTF-8");
            Gson gson = new Gson();
            Type bookListType = new TypeToken<ArrayList<Book>>(){}.getType();
            allBooks = gson.fromJson(json, bookListType);
        } catch (IOException e) {
            Toast.makeText(this, "Error loading book data", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void getCurrentUserAndRecommendBooks() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            fetchCoursesAndRecommendBooks(currentUser.getUid());
        } else {
            Toast.makeText(this, "Please log in to view recommendations", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchCoursesAndRecommendBooks(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(userId)
                .collection("enrolled_courses")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allRecommendedBooks.clear();
                        courseNames.clear();

                        for (DocumentSnapshot courseDoc : task.getResult()) {
                            String courseName = courseDoc.getString("courseName");
                            if (courseName != null && !courseName.isEmpty()) {
                                courseNames.add(courseName);
                                List<Book> courseBooks = BookRecommendation.recommendBooks(courseName, allBooks);
                                allRecommendedBooks.addAll(courseBooks);
                            }
                        }

                        // Remove duplicates and sort
                        allRecommendedBooks = BookRecommendation.processRecommendations(allRecommendedBooks);

                        // Update UI on main thread
                        runOnUiThread(() -> {
                            if (!allRecommendedBooks.isEmpty()) {
                                updateDisplayedBooks();
                                booksSection.setVisibility(View.VISIBLE);
                                booksRecyclerView.setVisibility(View.VISIBLE);
                                Toast.makeText(userDeatils.this, "Recommended books loaded successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                booksSection.setVisibility(View.VISIBLE);
                                booksRecyclerView.setVisibility(View.GONE);
                                Toast.makeText(userDeatils.this, "No recommended books found", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(userDeatils.this, "Error loading courses", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    private void updateDisplayedBooks() {
        int count = Integer.parseInt(countSpinner.getSelectedItem().toString());
        List<Book> booksToShow = allRecommendedBooks.subList(0, Math.min(count, allRecommendedBooks.size()));
        bookAdapter.updateBooks(booksToShow);
        booksRecyclerView.setVisibility(View.VISIBLE); // Ensure RecyclerView is visible
    }

    private void loadUserData() {
        String userId = firebaseUser.getUid();
        DocumentReference docRef = firestore.collection("users").document(userId);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(userDeatils.this, "Error in loading data", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (value != null && value.exists()) {
                    String name = value.getString("Name");
                    String email = value.getString("Email");
                    String roll = value.getString("studentId");
                    String profileImageUrl = value.getString("profileImageUrl");

                    welcome.setText("Hi " + name);
                    userEmail.setText(email);
                    userName.setText(name);
                    userRoll.setText(roll);

                    // Load profile image
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(userDeatils.this)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.user)
                                .error(R.drawable.user)
                                .into(profileImage);
                    } else {
                        profileImage.setImageResource(R.drawable.user);
                    }
                } else {
                    Toast.makeText(userDeatils.this, "No data found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showEnrolledCourses() {
        firestore.collection("users").document(currentUserId)
                .collection("enrolled_courses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "No enrolled courses found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    StringBuilder courses = new StringBuilder("Enrolled Courses:\n\n");
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String courseName = document.getString("courseName");
                        if (courseName != null) {
                            courses.append("• ").append(courseName).append("\n");
                        }
                    }

                    new AlertDialog.Builder(this)
                            .setTitle("Your Enrolled Courses")
                            .setMessage(courses.toString())
                            .setPositiveButton("OK", null)
                            .show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading enrolled courses", Toast.LENGTH_SHORT).show();
                });
    }

    //new sections

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            uploadImageToFirebase(selectedImageUri);
        }
    }
    private void uploadImageToFirebase(Uri imageUri) {
        progressDialog.show();

        // Initialize storage reference if not already done
        if (storageReference == null) {
            storageReference = storage.getReference();
        }

        String imageFileName = "profile_images/" + currentUserId + "/" + UUID.randomUUID().toString() + ".jpg";
        
        StorageReference imageRef = storageReference.child(imageFileName);
        
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get download URL after successful upload
                    imageRef.getDownloadUrl()
                            .addOnSuccessListener(imageUrl -> {
                                saveImageUrlToFirestore(imageUrl.toString());
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(this, "Failed to get image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void saveImageUrlToFirestore(String imageUrl) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> updates = new HashMap<>();
        updates.put("profileImageUrl", imageUrl); // Use consistent field name
        updates.put("profileImg", imageUrl); // Also save with the original field name for compatibility

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Profile image uploaded successfully!", Toast.LENGTH_SHORT).show();
                    // Reload user data to show the new image
                    loadUserData();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to update Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Permission handling methods
    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Permission denied. Cannot access images.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
