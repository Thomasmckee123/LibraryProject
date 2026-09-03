package com.example.bookshop.order;

import com.example.bookshop.book.Book;
import com.example.bookshop.book.InsufficientStockException;
import com.example.bookshop.cart.Cart;
import com.example.bookshop.cart.CartItem;
import com.example.bookshop.cart.CartRepository;
import com.example.bookshop.config.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;

/**
 * Turns a cart into a paid order.
 *
 * <p>Checkout is the one place stock is decided - see backend/CLAUDE.md - so
 * every line is verified against current stock before any line is
 * decremented. If line three is short, lines one and two must be untouched;
 * that is why this class checks everything first and mutates second, rather
 * than decrementing as it iterates.
 *
 * <p>The whole method is {@code @Transactional}: either the order is written
 * and the stock decremented together, or neither happens. A checkout that
 * takes stock without producing an order is the worst bug this app can have.
 */
@Service
public class CheckoutService {

    private static final String REFERENCE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int REFERENCE_LENGTH = 8;

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final SecureRandom random = new SecureRandom();

    public CheckoutService(CartRepository cartRepository, OrderRepository orderRepository) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderResponse checkout(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> NotFoundException.cart(cartId));

        if (cart.isEmpty()) {
            throw new IllegalArgumentException("cart " + cartId + " has no items to check out");
        }

        // Verify every line can be filled before touching any stock.
        for (CartItem item : cart.getItems()) {
            Book book = item.getBook();
            if (item.getQuantity() > book.getStock()) {
                throw new InsufficientStockException(
                        book.getIsbn(), item.getQuantity(), book.getStock());
            }
        }

        // Every line is now known to be fillable, so it is safe to mutate.
        Order order = new Order(generateReference(), cart.getCustomer(), Instant.now());
        for (CartItem item : cart.getItems()) {
            Book book = item.getBook();
            book.reduceStock(item.getQuantity());
            order.addLine(new OrderLine(order, book, item.getQuantity()));
        }

        cart.clear();
        Order saved = orderRepository.save(order);
        return OrderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse findOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> NotFoundException.order(id));
        return OrderResponse.from(order);
    }

    /** A fake payment confirmation - see backend/CLAUDE.md, payments are simulated. */
    private String generateReference() {
        StringBuilder reference = new StringBuilder("BND-");
        for (int i = 0; i < REFERENCE_LENGTH; i++) {
            reference.append(REFERENCE_CHARS.charAt(random.nextInt(REFERENCE_CHARS.length())));
        }
        return reference.toString();
    }
}
