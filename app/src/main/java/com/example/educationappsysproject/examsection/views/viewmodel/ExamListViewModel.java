package com.example.educationappsysproject.examsection.views.viewmodel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


import com.example.educationappsysproject.examsection.views.Model.ExamListModel;
import com.example.educationappsysproject.examsection.views.repository.ExamListRepository;

import java.util.List;
public class ExamListViewModel extends ViewModel implements ExamListRepository.onFirestoreTaskComplete {
    private MutableLiveData<List<ExamListModel>> examListLiveData= new MutableLiveData<>();
    private ExamListRepository repository= new ExamListRepository(this);

    public MutableLiveData<List<ExamListModel>> getExamListLiveData() {
        return examListLiveData;
    }

    public ExamListViewModel()
    {
        repository.getExamData();
    }
    @Override
    public void examDataLoaded(List<ExamListModel> examListModels) {
        examListLiveData.setValue(examListModels);
    }

    @Override
    public void onError(Exception e) {
        Log.d("ExamError","onError"+e.getMessage());
    }
}