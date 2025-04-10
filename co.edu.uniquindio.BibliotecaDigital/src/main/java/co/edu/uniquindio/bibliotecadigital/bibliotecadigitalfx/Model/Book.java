package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Enum.BookStatus;

public class Book {
    private String title;
    private String author;
    private int year;
    private String category;
    private BookStatus status;
    private double averageRating;
    private int totalRatings;
    private int ratingSum;

    public Book(String title, String author, int year, String category) {
        this.title = title;
        this.author = author;
        this.year = year;
        this.category = category;
        this.status = BookStatus.AVAILABLE;
        this.averageRating = 0.0;
        this.totalRatings = 0;
        this.ratingSum = 0;
    }

    // Getters and setters
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getYear() { return year; }
    public String getCategory() { return category; }
    public BookStatus getStatus() { return status; }
    public double getAverageRating() { return averageRating; }

    public void setStatus(BookStatus status) {
        this.status = status;
    }

    public void addRating(int stars) {
        ratingSum += stars;
        totalRatings++;
        averageRating = (double) ratingSum / totalRatings;
    }

    @Override
    public String toString() {
        return title + " by " + author + " (" + year + ") - " + category +
                " | Rating: " + String.format("%.2f", averageRating) +
                " | Status: " + status;
    }
}
