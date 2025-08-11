//Users
package com.example.educationappsysproject.homepage.models;

public class Users {
    private String userId;
    private String name;
    private String email;
    private String studentId;
    private String lastMessage; // New field for the last message
    private Long lastMessageTimestamp; // New field for the last message timestamp
    private String profileImageUrl; // Profile image URL
    private Boolean isAdmin; // Admin status

    // Empty constructor for Firestore
    public Users() {}

    public Users(String userId, String name, String email, String studentId, String lastMessage, Long lastMessageTimestamp) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.studentId = studentId;
        this.lastMessage = lastMessage;
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public Users(String userId, String name, String email, String studentId, String lastMessage, Long lastMessageTimestamp, String profileImageUrl, Boolean isAdmin) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.studentId = studentId;
        this.lastMessage = lastMessage;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.profileImageUrl = profileImageUrl;
        this.isAdmin = isAdmin;
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
    
    public String getLastMessage() { 
        return lastMessage; 
    }
    
    public void setLastMessage(String lastMessage) { 
        this.lastMessage = lastMessage; 
    }

    public Long getLastMessageTimestamp() { 
        return lastMessageTimestamp; 
    }
    
    public void setLastMessageTimestamp(Long lastMessageTimestamp) { 
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public Boolean isAdmin() {
        return isAdmin != null ? isAdmin : false;
    }

    public void setAdmin(Boolean admin) {
        isAdmin = admin;
    }
}
