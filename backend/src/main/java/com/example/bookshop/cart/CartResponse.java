package com.example.bookshop.cart;

import java.math.BigDecimal;
import java.util.List;

/** What a cart looks like over the API. Never the {@link Cart} entity itself. */
public record CartResponse(
        Long id,
        List<CartLineResponse> items,
        BigDecimal total) {
}
