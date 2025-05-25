package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Enum.BookStatus;

public class Book {
    private String idBook;
    private String title;
    private String author;
    private int year;
    private String category;
    private BookStatus status;
    private double averageRating;
    private int totalRatings;
    private int ratingSum;

    public Book(String id, String title, String author, int year, String category) {
        this.idBook = id;
        this.title = title;
        this.author = author;
        this.year = year;
        this.category = category;
        this.status = BookStatus.AVAILABLE;
        this.averageRating = 0.0;
        this.totalRatings = 0;
        this.ratingSum = 0;
    }

    public Book(){}

    public Book(String title) {
        this.title = title;
        this.author = "";
        this.category = "";
    }

    public String getIdBook() {
        return idBook;
    }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getYear() { return year; }
    public String getCategory() { return category; }
    public BookStatus getStatus() { return status; }
    public double getAverageRating() { return averageRating; }

    public void setIdBook(String idBook) {
        this.idBook = idBook;
    }

    public void setStatus(BookStatus status) {
        this.status = status;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public void setTotalRatings(int totalRatings) {
        this.totalRatings = totalRatings;
    }

    public void setRatingSum(int ratingSum) {
        this.ratingSum = ratingSum;
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
