package com.example.educationappsysproject.homepage.models;

public class ChatMessage {
    private String senderId;
    private String receiverId;
    private String message;
    private Long timestamp;
    private String type; // Optional: for future message types like image, file, etc.

    // Empty constructor for Firestore
    public ChatMessage() {}

    public ChatMessage(String senderId, String receiverId, String message, Long timestamp) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = timestamp;
        this.type = "text"; // Default type
    }

    // Getters and Setters
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}