package com.example.educationappsysproject.homepage.models;



public class Message {
    private String content;
    private boolean isUser; // true = user, false = bot

    public Message(String content, boolean isUser) {
        this.content = content;
        this.isUser = isUser;
    }

    public String getContent() {
        return content;
    }

    public boolean isUser() {
        return isUser;
}
}
