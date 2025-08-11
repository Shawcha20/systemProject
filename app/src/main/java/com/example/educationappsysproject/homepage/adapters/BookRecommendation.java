package com.example.educationappsysproject.homepage.adapters;

import com.example.educationappsysproject.homepage.models.Book;

import java.util.*;
import java.util.stream.Collectors;

public class BookRecommendation {
    public static List<Book> recommendBooks(String courseName, List<Book> allBooks) {
        List<Book> recommendedBooks = new ArrayList<>();

        for (Book book : allBooks) {
            double similarity = cosineSimilarity(
                    courseName.toLowerCase(),
                    book.getTitle().toLowerCase()
            );
            book.setSimilarityScore(similarity);

            if (similarity > 0.1) {
                recommendedBooks.add(book);
            }
        }

        recommendedBooks.sort((b1, b2) ->
                Double.compare(b2.getSimilarityScore(), b1.getSimilarityScore()));

        return recommendedBooks;
    }

    public static List<Book> processRecommendations(List<Book> books) {
        // Remove duplicates by title and keep highest similarity version
        Map<String, Book> uniqueBooks = new HashMap<>();
        for (Book book : books) {
            Book existing = uniqueBooks.get(book.getTitle());
            if (existing == null || book.getSimilarityScore() > existing.getSimilarityScore()) {
                uniqueBooks.put(book.getTitle(), book);
            }
        }

        // Sort by similarity
        return uniqueBooks.values().stream()
                .sorted((b1, b2) -> Double.compare(b2.getSimilarityScore(), b1.getSimilarityScore()))
                .collect(Collectors.toList());
    }

    private static double cosineSimilarity(String text1, String text2) {
        Map<String, Integer> freq1 = getWordFrequency(text1);
        Map<String, Integer> freq2 = getWordFrequency(text2);

        Set<String> allWords = new HashSet<>(freq1.keySet());
        allWords.addAll(freq2.keySet());

        double dotProduct = 0;
        for (String word : allWords) {
            dotProduct += freq1.getOrDefault(word, 0) * freq2.getOrDefault(word, 0);
        }

        double mag1 = Math.sqrt(freq1.values().stream().mapToDouble(v -> v * v).sum());
        double mag2 = Math.sqrt(freq2.values().stream().mapToDouble(v -> v * v).sum());

        return (mag1 == 0 || mag2 == 0) ? 0 : dotProduct / (mag1 * mag2);
    }

    private static Map<String, Integer> getWordFrequency(String text) {
        Map<String, Integer> freq = new HashMap<>();
        String[] words = text.split("\\s+");

        for (String word : words) {
            word = word.replaceAll("[^a-zA-Z]", "").toLowerCase();
            if (!word.isEmpty()) {
                freq.put(word, freq.getOrDefault(word, 0) + 1);
            }
        }
        return freq;
    }
}
