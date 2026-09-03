package com.example.bookshop.cart;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/** Body of {@code POST /api/carts/{id}/items}. */
public record AddItemRequest(
        @NotBlank String isbn,
        @Positive int quantity) {
}
