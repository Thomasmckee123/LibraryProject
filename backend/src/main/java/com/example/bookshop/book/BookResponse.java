package com.example.bookshop.book;

import java.math.BigDecimal;

/**
 * What the API returns for a book. Never return {@link Book} itself from a
 * controller - see backend/CLAUDE.md, "Entities don't leave the service
 * layer."
 */
public record BookResponse(String isbn, String title, String author, BigDecimal price, int stock) {

    public static BookResponse from(Book book) {
        return new BookResponse(book.getIsbn(), book.getTitle(), book.getAuthor(), book.getPrice(), book.getStock());
    }
}
