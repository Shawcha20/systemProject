package com.example.educationappsysproject.examsection.views.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.educationappsysproject.examsection.views.Model.QuestionModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
public class QuestionRepository {
    private FirebaseFirestore firebaseFirestore;
    private String examId;
    private HashMap<String,Long> resultMap= new HashMap<>();
    private OnQuestionLoad onQuestionLoad;
    private OnResultAdded onResultAdded;
    private  OnResultLoad onResultLoad;
    private String currentUserId=FirebaseAuth.getInstance().getCurrentUser().getUid();
    public void getResults()
    {
        firebaseFirestore.collection("Exam").document(examId).collection("results").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                 resultMap.put("correct",task.getResult().getLong("correct"));
                 resultMap.put("notAnswered",task.getResult().getLong("not answered"));
                 resultMap.put("wrong",task.getResult().getLong("wrong"));
                 onResultLoad.onResultLoad(resultMap);
                }
                else{
                    onResultLoad.onError(task.getException());
                }
            }
        });
    }
    public void addResults(HashMap<String,Object>resultMap){
        firebaseFirestore.collection("Exam").document(examId).collection("results").document(currentUserId).set(resultMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    onResultAdded.onSubmit();
                }
                else{
                    onResultAdded.onError(task.getException());

                }
            }
        });
    }

    public void setExamId(String examId) {
        this.examId = examId;
    }

    public  QuestionRepository(OnQuestionLoad onQuestionLoad ,OnResultAdded onResultAdded, OnResultLoad onResultLoad){
        firebaseFirestore = FirebaseFirestore.getInstance();
        this.onQuestionLoad=onQuestionLoad;
        this.onResultAdded=onResultAdded;
        this.onResultLoad=onResultLoad;
    }

    public void getQuestions(){
        firebaseFirestore.collection("Exam").document(examId).collection("Question").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull  Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            onQuestionLoad.onLoad(task.getResult().toObjects(QuestionModel.class));
                        }else{
                            onQuestionLoad.onError(task.getException());
                        }
                    }
                });
    }

    public interface OnResultLoad{
        void onResultLoad(HashMap<String , Long> resultMap);
        void onError(Exception e);
    }

    public interface OnQuestionLoad{
        void onLoad(List<QuestionModel> questionModels);
        void onError(Exception e);
    }

    public interface OnResultAdded{
        boolean onSubmit();
        void onError(Exception e);
    }
}
