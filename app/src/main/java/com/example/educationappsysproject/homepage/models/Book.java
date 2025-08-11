package com.example.educationappsysproject.homepage.models;

public class Book {
    private String title;
    private String author;
    private String description;
    private String language;
    private double similarityScore;

    public Book() {
        // Required empty constructor for Firestore
    }

    public Book(String title, String author, String description, String language) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.language = language;
    }

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
}
}
