package com.example.educationappsysproject.homepage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.educationappsysproject.R;

import com.example.educationappsysproject.examsection.views.ExamHome;
import com.example.educationappsysproject.videosection.VideoHome;

public class courseDetails extends AppCompatActivity {

    CardView video , exam , pdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_details);
        video = findViewById(R.id.videoCard);
        exam = findViewById(R.id.examCard);
        pdf = findViewById(R.id.pdfCard);

        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(courseDetails.this , VideoHome.class);
                startActivity(i);
            }
        });
        exam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(courseDetails.this , ExamHome.class);
                startActivity(i);
            }
        });

        pdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(courseDetails.this , courseDetails.class);
                startActivity(i);
            }
        });
    }
}