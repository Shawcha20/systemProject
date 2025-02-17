package com.example.educationappsysproject.homepage.models;

public class Users {
    private String userId;
    private String name;
    private String email;
    private String studentId;




    // Empty constructor for Firestore
    public Users() {}

    public Users(String userId, String name, String email, String studentId) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.studentId = studentId;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
}