package com.example.educationappsysproject.admin.addcourse_folder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.educationappsysproject.R;

public class addCourseName extends AppCompatActivity {

    public Button gotoVideo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_course);
        gotoVideo=findViewById(R.id.going_to_video);
        gotoVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(getApplicationContext(), uploadVideoPic.class);
                startActivity(intent);
                finish();
            }
        });
    }
}