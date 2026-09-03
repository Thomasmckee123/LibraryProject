package com.example.bookshop.config;

/** Something was addressed by id or isbn and does not exist. Maps to 404. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException book(String isbn) {
        return new NotFoundException("No book with isbn " + isbn);
    }

    public static NotFoundException cart(Long id) {
        return new NotFoundException("No cart with id " + id);
    }

    public static NotFoundException order(Long id) {
        return new NotFoundException("No order with id " + id);
    }
}
