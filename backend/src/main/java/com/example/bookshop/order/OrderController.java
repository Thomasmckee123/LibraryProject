package com.example.bookshop.order;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Checkout and order lookup. The checkout URL lives under {@code /api/carts}
 * because that is what the client is turning into an order, but the logic is
 * order logic, so it stays in this package - see backend/CLAUDE.md.
 *
 * <p>Thin by design: this parses the request and hands off to
 * {@link CheckoutService}. All the business rules - transactional stock
 * checks, price freezing, cart clearing - live there so they can be unit
 * tested without HTTP.
 */
@RestController
public class OrderController {

    private final CheckoutService checkoutService;

    public OrderController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/api/carts/{cartId}/checkout")
    public ResponseEntity<OrderResponse> checkout(@PathVariable Long cartId) {
        OrderResponse order = checkoutService.checkout(cartId);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/api/orders/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        return checkoutService.findOrder(id);
    }

    /**
     * A customer's order history, newest first.
     *
     * <p>{@code customerId} is a required parameter rather than something
     * read from a session, because there are no sessions yet - see issue #17.
     * Taking it explicitly keeps the gap visible at the API boundary instead
     * of hiding a hardcoded demo customer inside the service. When accounts
     * land, this parameter goes away and the id comes from the request's
     * principal; nothing else in this class changes.
     */
    @GetMapping("/api/orders")
    public List<OrderResponse> listOrders(@RequestParam Long customerId) {
        return checkoutService.findOrdersForCustomer(customerId);
    }
}
