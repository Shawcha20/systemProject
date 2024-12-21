package com.example.educationappsysproject.examsection.views.Model;

import com.google.firebase.firestore.DocumentId;

public class ExamListModel {

    @DocumentId
    private String examId;
    private String Title,Image;
    private long Questions;

    public String getExamId() {
        return examId;
    }

    public void setExamId(String examId) {
        this.examId = examId;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        this.Title = title;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        this.Image = image;
    }

    public long getQuestions() {
        return Questions;
    }

    public void setQuestions(long questions) {
        this.Questions = questions;
    }

    public ExamListModel(){}
    public ExamListModel(String examId, String title, String image, String difficulty, long questions) {
        this.examId = examId;
        this.Title = title;
        this.Image = image;

        this.Questions = questions;
    }
}
