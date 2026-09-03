package com.example.bookshop.order;

import com.example.bookshop.book.Book;
import com.example.bookshop.book.InsufficientStockException;
import com.example.bookshop.cart.Cart;
import com.example.bookshop.cart.CartRepository;
import com.example.bookshop.config.NotFoundException;
import com.example.bookshop.customer.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CheckoutService} against mocked repositories.
 *
 * <p>The scenario in
 * {@link #checkoutWithInsufficientStockLeavesEarlierBooksUntouched()} is the
 * one that matters most: it proves stock is verified for every line before
 * any line is decremented, which is the bug backend/CLAUDE.md calls out as
 * the worst this app can have.
 */
@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrderRepository orderRepository;

    private CheckoutService checkoutService;
    private Customer customer;

    @BeforeEach
    void setUp() {
        checkoutService = new CheckoutService(cartRepository, orderRepository);
        customer = new Customer("Ada Lovelace", "ada@example.com");
    }

    @Test
    void checkoutDecrementsStockCreatesOrderAndClearsCart() {
        Book dune = new Book("111", "Dune", "Frank Herbert", new BigDecimal("9.99"), 5);
        Book emma = new Book("222", "Emma", "Jane Austen", new BigDecimal("6.50"), 3);
        Cart cart = new Cart(customer);
        cart.addItem(dune, 2);
        cart.addItem(emma, 1);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = checkoutService.checkout(1L);

        assertThat(response.status()).isEqualTo(OrderStatus.PAID);
        assertThat(response.reference()).matches("BND-[A-Z0-9]{8}");
        assertThat(response.lines()).hasSize(2);
        assertThat(response.total()).isEqualByComparingTo(new BigDecimal("26.48"));

        assertThat(dune.getStock()).isEqualTo(3);
        assertThat(emma.getStock()).isEqualTo(2);
        assertThat(cart.isEmpty()).isTrue();
    }

    @Test
    void checkoutOnEmptyCartThrowsBadRequest() {
        Cart cart = new Cart(customer);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> checkoutService.checkout(1L))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(orderRepository);
    }

    @Test
    void checkoutOnUnknownCartThrowsNotFound() {
        when(cartRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> checkoutService.checkout(99L))
                .isInstanceOf(NotFoundException.class);

        verifyNoInteractions(orderRepository);
    }

    @Test
    void checkoutWithInsufficientStockThrows() {
        Book hobbit = new Book("333", "The Hobbit", "J. R. R. Tolkien", new BigDecimal("10.50"), 1);
        Cart cart = new Cart(customer);
        cart.addItem(hobbit, 5);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> checkoutService.checkout(1L))
                .isInstanceOf(InsufficientStockException.class);

        verifyNoInteractions(orderRepository);
    }

    @Test
    void checkoutWithInsufficientStockLeavesEarlierBooksUntouched() {
        Book dune = new Book("111", "Dune", "Frank Herbert", new BigDecimal("9.99"), 5);
        Book emma = new Book("222", "Emma", "Jane Austen", new BigDecimal("6.50"), 3);
        Book hobbit = new Book("333", "The Hobbit", "J. R. R. Tolkien", new BigDecimal("10.50"), 1);
        Cart cart = new Cart(customer);
        // First two lines are fully fillable; the third asks for more than is on the shelf.
        cart.addItem(dune, 2);
        cart.addItem(emma, 1);
        cart.addItem(hobbit, 5);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> checkoutService.checkout(1L))
                .isInstanceOf(InsufficientStockException.class);

        // Books 1 and 2 must be untouched: verify-all-then-mutate, not decrement-as-you-go.
        assertThat(dune.getStock()).isEqualTo(5);
        assertThat(emma.getStock()).isEqualTo(3);
        assertThat(hobbit.getStock()).isEqualTo(1);
        assertThat(cart.isEmpty()).isFalse();
        verifyNoInteractions(orderRepository);
    }

    @Test
    void findOrderReturnsMappedResponse() {
        Book dune = new Book("111", "Dune", "Frank Herbert", new BigDecimal("9.99"), 5);
        Order order = new Order("BND-ABCDEFGH", customer, java.time.Instant.now());
        order.addLine(new OrderLine(order, dune, 2));
        when(orderRepository.findById(7L)).thenReturn(Optional.of(order));

        OrderResponse response = checkoutService.findOrder(7L);

        assertThat(response.reference()).isEqualTo("BND-ABCDEFGH");
        assertThat(response.lines()).hasSize(1);
        assertThat(response.total()).isEqualByComparingTo(new BigDecimal("19.98"));
    }

    @Test
    void findOrderOnUnknownIdThrowsNotFound() {
        when(orderRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> checkoutService.findOrder(404L))
                .isInstanceOf(NotFoundException.class);
    }
}
