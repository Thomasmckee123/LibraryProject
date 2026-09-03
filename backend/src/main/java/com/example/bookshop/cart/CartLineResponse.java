package com.example.bookshop.cart;

import java.math.BigDecimal;

/** One line of a cart: a book, how many, and what that comes to. */
public record CartLineResponse(
        String isbn,
        String title,
        String author,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal) {
}
