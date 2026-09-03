package com.example.bookshop.order;

import com.example.bookshop.book.Book;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * One book on an order, at the price it was actually sold for.
 *
 * <p>{@code unitPrice} is a copy, not a reference. Reading the price off
 * {@link Book} instead would mean repricing history every time the shop
 * changes a price.
 */
@Entity
@Table(name = "order_lines")
public class OrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    /** Snapshot of the title, so an order still reads correctly if a book is renamed. */
    @Column(nullable = false, updatable = false)
    private String titleAtPurchase;

    @Column(nullable = false, updatable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, updatable = false)
    private int quantity;

    protected OrderLine() {
    }

    public OrderLine(Order order, Book book, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        this.order = order;
        this.book = book;
        this.titleAtPurchase = book.getTitle();
        this.unitPrice = book.getPrice();
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public Book getBook() {
        return book;
    }

    public String getTitleAtPurchase() {
        return titleAtPurchase;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
