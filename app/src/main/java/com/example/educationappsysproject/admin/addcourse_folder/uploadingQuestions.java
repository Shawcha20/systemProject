package com.example.educationappsysproject.admin.addcourse_folder;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.educationappsysproject.R;
import com.example.educationappsysproject.admin.adminHomePage;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class uploadingQuestions extends AppCompatActivity {

    private Button buttonDone;
    private LinearLayout questionContainer;
    private int examNum;
    private String documentId, examDocumentId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uploading_questions);

        buttonDone = findViewById(R.id.buttonDone);
        questionContainer = findViewById(R.id.questionContainer);
        db = FirebaseFirestore.getInstance();

        examNum = getIntent().getIntExtra("Number", 0);
        documentId = getIntent().getStringExtra("documentId");
        examDocumentId = getIntent().getStringExtra("examDocumentId");

        if (examNum > 0 && documentId != null && examDocumentId != null) {
            for (int i = 1; i <= examNum; i++) {
                addQuestionView(i);
            }
        } else {
            Toast.makeText(this, "Invalid data received", Toast.LENGTH_SHORT).show();
            finish();
        }

        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadQuestionsToFirebase();
            }
        });
    }

    private void addQuestionView(int questionNumber) {
        LinearLayout questionLayout = new LinearLayout(this);
        questionLayout.setOrientation(LinearLayout.VERTICAL);
        questionLayout.setPadding(16, 16, 16, 16);

        TextView questionLabel = new TextView(this);
        questionLabel.setText("Question " + questionNumber + ":");
        questionLabel.setTextSize(18);
        questionLabel.setGravity(Gravity.START);
        questionLayout.addView(questionLabel);

        EditText questionInput = new EditText(this);
        questionInput.setHint("Enter question " + questionNumber);
        questionLayout.addView(questionInput);

        EditText answerInput = new EditText(this);
        answerInput.setHint("Enter answer for question " + questionNumber);
        questionLayout.addView(answerInput);

        for (int i = 1; i <= 4; i++) {
            EditText optionInput = new EditText(this);
            optionInput.setHint("Option " + i);
            questionLayout.addView(optionInput);
        }

        EditText timerInput = new EditText(this);
        timerInput.setHint("Timer for Question " + questionNumber + " (in seconds)");
        questionLayout.addView(timerInput);

        questionContainer.addView(questionLayout);
    }

    boolean hasError = false;
    private void uploadQuestionsToFirebase() {
        int questionCount = questionContainer.getChildCount();


        for (int i = 0; i < questionCount; i++) {
            View questionView = questionContainer.getChildAt(i);

            if (questionView instanceof LinearLayout) {
                LinearLayout questionLayout = (LinearLayout) questionView;

                // Fetching views and their values
                String questionText = ((EditText) questionLayout.getChildAt(1)).getText().toString().trim();
                String answerText = ((EditText) questionLayout.getChildAt(2)).getText().toString().trim();

                if (questionText.isEmpty() || answerText.isEmpty()) {
                    hasError = true;
                    Toast.makeText(this, "Question and Answer fields cannot be empty!", Toast.LENGTH_SHORT).show();
                    continue; // Skip to the next question
                }

                Map<String, Object> questionData = new HashMap<>();
                questionData.put("question", questionText);
                questionData.put("answer", answerText);

                // Fetching option values
                for (int j = 3, optionNum = 1; j <= 6; j++, optionNum++) {
                    String optionText = ((EditText) questionLayout.getChildAt(j)).getText().toString().trim();
                    if (optionText.isEmpty()) {
                        hasError = true;
                        Toast.makeText(this, "Options cannot be empty!", Toast.LENGTH_SHORT).show();
                        break; // Exit current question processing
                    }
                    questionData.put("option" + optionNum, optionText);
                }

                // Fetching timer value
                String timer = ((EditText) questionLayout.getChildAt(7)).getText().toString().trim();
                if (timer.isEmpty()) {
                    hasError = true;
                    Toast.makeText(this, "Timer cannot be empty!", Toast.LENGTH_SHORT).show();
                    continue; // Skip to the next question
                }
                questionData.put("timer", timer);

                // Uploading question data
                db.collection("course")
                        .document(documentId)
                        .collection("exam")
                        .document(examDocumentId)
                        .collection("questions")
                        .add(questionData)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(this, "Question uploaded successfully!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            hasError = true;
                            Toast.makeText(this, "Error uploading question: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        }

        if (!hasError) {
            // Navigate to the admin home page if all uploads were successful
            Intent intent = new Intent(getApplicationContext(), adminHomePage.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Some questions failed to upload. Please check!", Toast.LENGTH_SHORT).show();
        }
    }

}
