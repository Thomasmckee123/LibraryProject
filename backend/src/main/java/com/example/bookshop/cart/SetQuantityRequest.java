package com.example.bookshop.cart;

import jakarta.validation.constraints.Positive;

/**
 * Sets a cart line to an absolute quantity.
 *
 * <p>Distinct from {@link AddItemRequest}, which is a delta. A stepper going
 * from 3 to 2 has no positive delta to send, so it needs this instead.
 */
public record SetQuantityRequest(@Positive int quantity) {
}
