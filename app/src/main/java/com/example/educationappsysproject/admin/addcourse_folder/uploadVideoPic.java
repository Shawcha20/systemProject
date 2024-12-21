package com.example.educationappsysproject.admin.addcourse_folder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.educationappsysproject.R;

public class uploadVideoPic extends AppCompatActivity {

    public Button goToQuestion;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upload_video);
        goToQuestion=findViewById(R.id.goToQuestion);
        goToQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(getApplicationContext(), uploadingQuestions.class);
                startActivity(intent);
                finish();
            }
        });
    }
}