package com.example.bookshop.cart;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cart endpoints. Stays thin: parse the request, call {@link CartService},
 * return the DTO it produces. Checkout is not here - see the order package.
 */
@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{id}")
    public CartResponse getCart(@PathVariable Long id) {
        return cartService.getCart(id);
    }

    @PostMapping("/{id}/items")
    public CartResponse addItem(@PathVariable Long id, @Valid @RequestBody AddItemRequest request) {
        return cartService.addItem(id, request);
    }

    @DeleteMapping("/{id}/items/{isbn}")
    public CartResponse removeItem(@PathVariable Long id, @PathVariable String isbn) {
        return cartService.removeItem(id, isbn);
    }
}
