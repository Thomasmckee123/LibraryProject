package com.example.bookshop.order;

import com.example.bookshop.book.Book;
import com.example.bookshop.book.BookRepository;
import com.example.bookshop.cart.Cart;
import com.example.bookshop.cart.CartRepository;
import com.example.bookshop.customer.Customer;
import com.example.bookshop.customer.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end checkout tests against the real H2 database - the path where a
 * transaction bug would hide, per backend/CLAUDE.md.
 *
 * <p>{@link #priceChangeAfterCheckoutDoesNotAlterOrderTotal()} is the
 * invariant the whole design exists to protect: an {@code OrderLine} freezes
 * its price at purchase time, so a later price change must never be able to
 * reach back into an already-placed order.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CheckoutIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    /**
     * {@code Cart.items} is a lazy collection. Reading it - even just
     * {@code isEmpty()} - needs an open Hibernate session, which the test
     * thread does not have once the HTTP call that did the checkout has
     * returned. A short-lived transaction just for the read keeps the
     * assertion honest without pulling the whole test into one transaction,
     * which would hide the setup data from the separate HTTP request thread.
     */
    private boolean cartIsEmpty(Long cartId) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        return Boolean.TRUE.equals(transactionTemplate.execute(
                status -> cartRepository.findById(cartId).orElseThrow().isEmpty()));
    }

    @Test
    void checkoutPersistsOrderDecrementsStockAndClearsCart() {
        Customer customer = customerRepository.save(new Customer("Grace Hopper", "grace@example.com"));
        Book book = bookRepository.save(
                new Book("9990000000001", "Compiler Design", "Grace Hopper", new BigDecimal("15.00"), 10));

        Cart cart = new Cart(customer);
        cart.addItem(book, 3);
        cart = cartRepository.save(cart);

        ResponseEntity<OrderResponse> response = restTemplate.postForEntity(
                "/api/carts/" + cart.getId() + "/checkout", null, OrderResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OrderResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(OrderStatus.PAID);
        assertThat(body.reference()).matches("BND-[A-Z0-9]{8}");
        assertThat(body.total()).isEqualByComparingTo(new BigDecimal("45.00"));

        // Order actually persisted.
        assertThat(orderRepository.findById(body.id())).isPresent();

        // Stock actually decremented in the DB, not just on the in-memory entity.
        Book reloadedBook = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(reloadedBook.getStock()).isEqualTo(7);

        // Cart actually emptied in the DB.
        assertThat(cartIsEmpty(cart.getId())).isTrue();
    }

    @Test
    void priceChangeAfterCheckoutDoesNotAlterOrderTotal() {
        Customer customer = customerRepository.save(new Customer("Katherine Johnson", "katherine@example.com"));
        Book book = bookRepository.save(
                new Book("9990000000002", "Orbital Mechanics", "Katherine Johnson", new BigDecimal("20.00"), 5));

        Cart cart = new Cart(customer);
        cart.addItem(book, 2);
        cart = cartRepository.save(cart);

        ResponseEntity<OrderResponse> checkoutResponse = restTemplate.postForEntity(
                "/api/carts/" + cart.getId() + "/checkout", null, OrderResponse.class);
        assertThat(checkoutResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long orderId = checkoutResponse.getBody().id();
        assertThat(checkoutResponse.getBody().total()).isEqualByComparingTo(new BigDecimal("40.00"));

        // Change the book's price well after the order was placed.
        Book reloadedBook = bookRepository.findById(book.getId()).orElseThrow();
        reloadedBook.setPrice(new BigDecimal("999.99"));
        bookRepository.save(reloadedBook);

        ResponseEntity<OrderResponse> refetched = restTemplate.getForEntity(
                "/api/orders/" + orderId, OrderResponse.class);

        assertThat(refetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(refetched.getBody().total()).isEqualByComparingTo(new BigDecimal("40.00"));
        assertThat(refetched.getBody().lines().get(0).unitPrice()).isEqualByComparingTo(new BigDecimal("20.00"));
    }

    @Test
    void checkoutOnEmptyCartReturns400() {
        Customer customer = customerRepository.save(new Customer("Empty Cart", "empty@example.com"));
        Cart cart = cartRepository.save(new Cart(customer));

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/carts/" + cart.getId() + "/checkout", null, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void checkoutOnUnknownCartReturns404() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/carts/999999/checkout", null, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void checkoutWithInsufficientStockReturns409AndLeavesStockUntouched() {
        Customer customer = customerRepository.save(new Customer("Short Stock", "short@example.com"));
        Book book = bookRepository.save(
                new Book("9990000000003", "Rare Book", "Someone Obscure", new BigDecimal("50.00"), 1));

        Cart cart = new Cart(customer);
        cart.addItem(book, 5);
        cart = cartRepository.save(cart);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/carts/" + cart.getId() + "/checkout", null, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        Book reloadedBook = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(reloadedBook.getStock()).isEqualTo(1);

        assertThat(cartIsEmpty(cart.getId())).isFalse();
    }

    @Test
    void getOrderOnUnknownIdReturns404() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/orders/999999", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    /**
     * The list endpoint has to freeze prices for the same reason the single
     * order endpoint does. It is a separate code path, so it gets its own
     * assertion rather than trusting that
     * {@link #priceChangeAfterCheckoutDoesNotAlterOrderTotal()} covers it -
     * a list built by joining back to Book would pass that test and fail
     * this one.
     */
    @Test
    void listedOrdersKeepTheirPricesWhenTheBookIsRepriced() {
        Customer customer = customerRepository.save(new Customer("Ada Lovelace", "ada@example.com"));
        Book book = bookRepository.save(
                new Book("9990000000004", "Analytical Engine", "Ada Lovelace", new BigDecimal("30.00"), 5));

        Cart cart = new Cart(customer);
        cart.addItem(book, 2);
        cart = cartRepository.save(cart);

        ResponseEntity<OrderResponse> checkout = restTemplate.postForEntity(
                "/api/carts/" + cart.getId() + "/checkout", null, OrderResponse.class);
        assertThat(checkout.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Book reloaded = bookRepository.findById(book.getId()).orElseThrow();
        reloaded.setPrice(new BigDecimal("1.00"));
        bookRepository.save(reloaded);

        ResponseEntity<OrderResponse[]> listed = restTemplate.getForEntity(
                "/api/orders?customerId=" + customer.getId(), OrderResponse[].class);

        assertThat(listed.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listed.getBody()).hasSize(1);
        assertThat(listed.getBody()[0].total()).isEqualByComparingTo(new BigDecimal("60.00"));
        assertThat(listed.getBody()[0].lines().get(0).unitPrice())
                .isEqualByComparingTo(new BigDecimal("30.00"));
    }

    @Test
    void listedOrdersComeBackNewestFirstAndOnlyForThatCustomer() {
        Customer mine = customerRepository.save(new Customer("Alan Turing", "alan@example.com"));
        Customer theirs = customerRepository.save(new Customer("Someone Else", "else@example.com"));
        Book book = bookRepository.save(
                new Book("9990000000005", "On Computable Numbers", "Alan Turing", new BigDecimal("11.00"), 20));

        String firstReference = placeOrder(mine, book, 1).reference();
        String secondReference = placeOrder(mine, book, 2).reference();
        String othersReference = placeOrder(theirs, book, 1).reference();

        ResponseEntity<OrderResponse[]> listed = restTemplate.getForEntity(
                "/api/orders?customerId=" + mine.getId(), OrderResponse[].class);

        assertThat(listed.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listed.getBody()).hasSize(2);
        assertThat(listed.getBody())
                .extracting(OrderResponse::reference)
                .containsExactly(secondReference, firstReference)
                .doesNotContain(othersReference);
    }

    /** Places one order the way a customer would - through checkout, not by writing an Order. */
    private OrderResponse placeOrder(Customer customer, Book book, int quantity) {
        Cart cart = new Cart(customer);
        cart.addItem(book, quantity);
        cart = cartRepository.save(cart);

        ResponseEntity<OrderResponse> response = restTemplate.postForEntity(
                "/api/carts/" + cart.getId() + "/checkout", null, OrderResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }
}
