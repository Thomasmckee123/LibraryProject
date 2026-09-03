package com.example.bookshop.order;

import java.math.BigDecimal;

/**
 * One purchased line, at the price it was actually sold for.
 *
 * <p>Built from {@link OrderLine}'s frozen fields, never from the current
 * {@code Book} price - see backend/CLAUDE.md.
 */
public record OrderLineResponse(
        String isbn,
        String titleAtPurchase,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal) {

    static OrderLineResponse from(OrderLine line) {
        return new OrderLineResponse(
                line.getBook().getIsbn(),
                line.getTitleAtPurchase(),
                line.getUnitPrice(),
                line.getQuantity(),
                line.lineTotal());
    }
}
