package com.example.educationappsysproject.examsection.views.viewmodel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


import com.example.educationappsysproject.examsection.views.Model.QuestionModel;
import com.example.educationappsysproject.examsection.views.repository.QuestionRepository;

import java.util.HashMap;
import java.util.List;


public class QuestionViewModel extends ViewModel implements QuestionRepository.OnQuestionLoad, QuestionRepository.OnResultAdded, QuestionRepository.OnResultLoad {
    private MutableLiveData<List<QuestionModel>> questionMutableLiveData;
    private QuestionRepository repository;
    private MutableLiveData<HashMap<String,Long>> resultMutableLiveData;
    public MutableLiveData<List<QuestionModel>> getQuestionMutableLiveData() {
        return questionMutableLiveData;
    }
    public QuestionViewModel()
    {
        questionMutableLiveData=new MutableLiveData<>();
        resultMutableLiveData= new MutableLiveData<>();
        repository= new QuestionRepository(this,this,this);
    }

    public void addResults(HashMap<String, Object>resultMap)
    {
        repository.addResults(resultMap);
    }
    public void getQuestions(){
        repository.getQuestions();
    }

    public void setExamId(String examId){
        repository.setExamId(examId);
    }

    @Override
    public void onLoad(List<QuestionModel> questionModels) {
        questionMutableLiveData.setValue(questionModels);
    }

    @Override
    public boolean onSubmit() {
        return true;
    }

    public MutableLiveData<HashMap<String, Long>> getResultMutableLiveData() {
        return resultMutableLiveData;
    }
    public void getResults(){
        repository.getResults();
    }
    @Override
    public void onResultLoad(HashMap<String, Long> resultMap) {
        resultMutableLiveData.setValue(resultMap);
    }

    @Override
    public void onError(Exception e) {
        Log.d("ExamError","onError:"+e.getMessage());
    }
}