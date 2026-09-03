package com.example.bookshop.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * An order as returned by the API. The entity never leaves the service layer
 * - see backend/CLAUDE.md - so this is built inside {@link CheckoutService}
 * while the persistence session is still open, which also keeps the lazy
 * {@code Order.lines} collection safe to read.
 */
public record OrderResponse(
        Long id,
        String reference,
        Long customerId,
        OrderStatus status,
        Instant placedAt,
        BigDecimal total,
        List<OrderLineResponse> lines) {

    static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getReference(),
                order.getCustomer().getId(),
                order.getStatus(),
                order.getPlacedAt(),
                order.total(),
                order.getLines().stream().map(OrderLineResponse::from).toList());
    }
}
