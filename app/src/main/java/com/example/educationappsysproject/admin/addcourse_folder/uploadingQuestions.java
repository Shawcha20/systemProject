package com.example.educationappsysproject.admin.addcourse_folder;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.educationappsysproject.R;

//import cn.pedant.SweetAlert.SweetAlertDialog;

public class uploadingQuestions extends AppCompatActivity {

    public Button buttonDone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_uploading_questions);
        buttonDone=findViewById(R.id.buttonDone);
        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                new SweetAlertDialog(uploadingQuestions.this, SweetAlertDialog.ERROR_TYPE)
//                        .setTitleText("oopps").setContentText("somethijng went wrong").show();

            }
        });
    }
}