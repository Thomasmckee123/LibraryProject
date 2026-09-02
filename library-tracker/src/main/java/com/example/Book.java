package com.example;

import java.util.Objects;

/**
 * Stage 1: title, author, isbn, and borrow state.
 *
 * <p>Two books are considered equal when they share an ISBN.
 */
public class Book {

    private String title;
    private String author;
    private final String isbn;
    private boolean isBorrowed;

    public Book(String title, String author, String isbn) {
        this(title, author, isbn, false);
    }

    public Book(String title, String author, String isbn, boolean isBorrowed) {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("isbn must not be blank");
        }
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

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setBorrowed(boolean isBorrowed) {
        this.isBorrowed = isBorrowed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Book other)) {
            return false;
        }
        return isbn.equals(other.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn);
    }

    @Override
    public String toString() {
        return "Book{title='" + title + "', author='" + author
                + "', isbn='" + isbn + "', borrowed=" + isBorrowed + "}";
    }
}
