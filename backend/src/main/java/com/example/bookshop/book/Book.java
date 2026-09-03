package com.example.bookshop.book;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A book for sale. Two books are equal when they share an ISBN.
 *
 * <p>Carried over from the lending exercise: ISBN-based equality, a validated
 * constructor, and a final isbn. Gained price and stock; lost the borrow flag.
 */
@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String isbn;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    /** DECIMAL, never a floating-point type - see backend/CLAUDE.md. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int stock;

    /** JPA requires a no-arg constructor; application code should not use it. */
    protected Book() {
    }

    public Book(String isbn, String title, String author, BigDecimal price, int stock) {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("isbn must not be blank");
        }
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("price must not be negative");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("stock must not be negative");
        }
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.price = price;
        this.stock = stock;
    }

    public Long getId() {
        return id;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setPrice(BigDecimal price) {
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("price must not be negative");
        }
        this.price = price;
    }

    public boolean isInStock() {
        return stock > 0;
    }

    /** Takes {@code quantity} off the shelf. Throws if there are not that many. */
    public void reduceStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (quantity > stock) {
            throw new InsufficientStockException(isbn, quantity, stock);
        }
        this.stock -= quantity;
    }

    public void restock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        this.stock += quantity;
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
        return "Book{isbn='" + isbn + "', title='" + title + "', stock=" + stock + "}";
    }
}
