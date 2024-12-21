package com.example.educationappsysproject.videosection;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.educationappsysproject.R;


public class VideoHome extends AppCompatActivity {

    View classOne;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_home);
        classOne = findViewById(R.id.videOne);

        classOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClassOneClick(VideoHome.this);
            }
        });
    }

    public void handleClassOneClick(Context context) {
        Intent intent = new Intent(context, ShowVideo.class);
        context.startActivity(intent);
    }
}
