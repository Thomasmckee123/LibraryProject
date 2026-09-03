package com.example.bookshop.book;

/** Thrown when an order asks for more copies than are on the shelf. */
public class InsufficientStockException extends RuntimeException {

    private final String isbn;
    private final int requested;
    private final int available;

    public InsufficientStockException(String isbn, int requested, int available) {
        super("Only %d copies of %s available, %d requested".formatted(available, isbn, requested));
        this.isbn = isbn;
        this.requested = requested;
        this.available = available;
    }

    public String getIsbn() {
        return isbn;
    }

    public int getRequested() {
        return requested;
    }

    public int getAvailable() {
        return available;
    }
}
