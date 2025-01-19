package com.example.educationappsysproject.examsection.views.repository;
import androidx.annotation.NonNull;

import com.example.educationappsysproject.examsection.views.Model.ExamListModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.List;

public class ExamListRepository {
    private  onFirestoreTaskComplete onFirestoreTaskComplete;
    private FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
    private CollectionReference reference=firebaseFirestore.collection("Exam");
    public ExamListRepository(onFirestoreTaskComplete onFirestoreTaskComplete)
    {
        this.onFirestoreTaskComplete=onFirestoreTaskComplete;
    }
    public void getExamData()
    {
        reference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    onFirestoreTaskComplete.examDataLoaded(task.getResult().toObjects(ExamListModel.class));
                }
                else {
                    onFirestoreTaskComplete.onError(task.getException());
                }
            }
        });
    }
    public interface  onFirestoreTaskComplete
    {
        void examDataLoaded(List<ExamListModel> examListModels);
        void onError(Exception e);
    }
}