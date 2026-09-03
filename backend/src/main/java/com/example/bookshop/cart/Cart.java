package com.example.bookshop.cart;

import com.example.bookshop.book.Book;
import com.example.bookshop.customer.Customer;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * An open basket. Holding a book in a cart reserves nothing - stock is only
 * decremented at checkout, so two people may hold the last copy at once.
 */
@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    protected Cart() {
    }

    public Cart(Customer customer) {
        this.customer = customer;
    }

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public Optional<CartItem> findItem(String isbn) {
        return items.stream()
                .filter(item -> item.getBook().getIsbn().equals(isbn))
                .findFirst();
    }

    /** Adds to an existing line for this book, or starts a new one. */
    public CartItem addItem(Book book, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        return findItem(book.getIsbn())
                .map(existing -> {
                    existing.increaseQuantity(quantity);
                    return existing;
                })
                .orElseGet(() -> {
                    CartItem item = new CartItem(this, book, quantity);
                    items.add(item);
                    return item;
                });
    }

    public boolean removeItem(String isbn) {
        return items.removeIf(item -> item.getBook().getIsbn().equals(isbn));
    }

    public void clear() {
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    /** Live total at current prices. An Order freezes its own copy at checkout. */
    public BigDecimal total() {
        return items.stream()
                .map(CartItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
