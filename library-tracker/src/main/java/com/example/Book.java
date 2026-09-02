package com.example;

public class Book {
    // Stage 1: title, author, isbn, and borrow state.
    private String title;
    private String author;
    private String isbn;
    private boolean isBorrowed;

    public Book(String title, String author, String isbn, boolean isBorrowed) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.isBorrowed = isBorrowed;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public boolean isBorrowed() {
        return isBorrowed;
    }

    public String setTitle(String title) {
        this.title = title;
        return title;
    }
    public String setAuthour(String author) {
        this.author = author;
        return author;
    }
    public String setIsbn(String isbn) {
        this.isbn = isbn;
        return isbn;
    }
    public boolean setIsBorrowed(boolean isBorrowed){
        this.isBorrowed = isBorrowed;
        return isBorrowed;
    }

}
