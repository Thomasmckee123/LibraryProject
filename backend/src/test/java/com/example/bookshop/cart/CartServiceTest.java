package com.example.bookshop.cart;

import com.example.bookshop.book.Book;
import com.example.bookshop.book.BookRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private BookRepository bookRepository;

    private CartService cartService;

    private Cart cart;
    private Book book;

    @BeforeEach
    void setUp() {
        cartService = new CartService(cartRepository, bookRepository);
        Customer customer = new Customer("Ada Lovelace", "ada@example.com");
        cart = new Cart(customer);
        book = new Book("978-0-13-468599-1", "Effective Java", "Joshua Bloch",
                new BigDecimal("45.00"), 5);
    }

    @Test
    void getCartReturnsLinesAndTotal() {
        cart.addItem(book, 2);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        CartResponse response = cartService.getCart(1L);

        assertThat(response.lines()).hasSize(1);
        CartLineResponse line = response.lines().get(0);
        assertThat(line.isbn()).isEqualTo(book.getIsbn());
        assertThat(line.title()).isEqualTo(book.getTitle());
        assertThat(line.author()).isEqualTo(book.getAuthor());
        assertThat(line.unitPrice()).isEqualByComparingTo("45.00");
        assertThat(line.quantity()).isEqualTo(2);
        assertThat(line.lineTotal()).isEqualByComparingTo("90.00");
        assertThat(response.total()).isEqualByComparingTo("90.00");
    }

    @Test
    void getCartUnknownIdThrowsNotFound() {
        when(cartRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cartService.getCart(99L));
    }

    @Test
    void addNewItemCreatesLine() {
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(bookRepository.findByIsbn(book.getIsbn())).thenReturn(Optional.of(book));

        CartResponse response = cartService.addItem(1L, new AddItemRequest(book.getIsbn(), 3));

        assertThat(response.lines()).hasSize(1);
        assertThat(response.lines().get(0).quantity()).isEqualTo(3);
        verify(cartRepository).save(cart);
    }

    @Test
    void addExistingItemMergesQuantity() {
        cart.addItem(book, 2);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(bookRepository.findByIsbn(book.getIsbn())).thenReturn(Optional.of(book));

        CartResponse response = cartService.addItem(1L, new AddItemRequest(book.getIsbn(), 3));

        assertThat(response.lines()).hasSize(1);
        assertThat(response.lines().get(0).quantity()).isEqualTo(5);
    }

    @Test
    void setQuantityLowersALineWithoutRemovingIt() {
        cart.addItem(book, 3);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(cart)).thenReturn(cart);

        CartResponse response = cartService.setQuantity(1L, book.getIsbn(),
                new SetQuantityRequest(2));

        assertThat(response.lines()).hasSize(1);
        assertThat(response.lines().get(0).quantity()).isEqualTo(2);
        assertThat(response.total()).isEqualByComparingTo("90.00");
    }

    @Test
    void setQuantityRaisesALine() {
        cart.addItem(book, 1);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(cart)).thenReturn(cart);

        CartResponse response = cartService.setQuantity(1L, book.getIsbn(),
                new SetQuantityRequest(4));

        assertThat(response.lines().get(0).quantity()).isEqualTo(4);
    }

    @Test
    void setQuantityDoesNotReduceStock() {
        cart.addItem(book, 3);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(cart)).thenReturn(cart);

        cartService.setQuantity(1L, book.getIsbn(), new SetQuantityRequest(1));

        assertThat(book.getStock()).isEqualTo(5);
    }

    @Test
    void setQuantityOnABookNotInTheCartThrowsNotFound() {
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        assertThrows(NotFoundException.class, () -> cartService.setQuantity(
                1L, "978-0-00-000000-0", new SetQuantityRequest(2)));
    }

    @Test
    void addItemDoesNotReduceStock() {
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(bookRepository.findByIsbn(book.getIsbn())).thenReturn(Optional.of(book));

        cartService.addItem(1L, new AddItemRequest(book.getIsbn(), 4));

        // Adding to a cart reserves nothing - stock is only touched at
        // checkout. This is the rule the whole package exists to protect.
        assertThat(book.getStock()).isEqualTo(5);
    }

    @Test
    void addItemUnknownIsbnThrowsNotFound() {
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(bookRepository.findByIsbn("unknown-isbn")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> cartService.addItem(1L, new AddItemRequest("unknown-isbn", 1)));
    }

    @Test
    void addItemRejectsOutOfStockBookWithoutTouchingIt() {
        Book outOfStock = new Book("978-0-00-000000-0", "Ghost Edition", "Nobody",
                new BigDecimal("10.00"), 0);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(bookRepository.findByIsbn(outOfStock.getIsbn())).thenReturn(Optional.of(outOfStock));

        assertThrows(IllegalArgumentException.class,
                () -> cartService.addItem(1L, new AddItemRequest(outOfStock.getIsbn(), 1)));
        assertThat(cart.getItems()).isEmpty();
        assertThat(outOfStock.getStock()).isEqualTo(0);
    }

    @Test
    void removeItemDeletesLine() {
        cart.addItem(book, 2);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        CartResponse response = cartService.removeItem(1L, book.getIsbn());

        assertThat(response.lines()).isEmpty();
        assertThat(response.total()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(cartRepository).save(cart);
    }

    @Test
    void removeItemNotInCartThrowsNotFound() {
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        assertThrows(NotFoundException.class, () -> cartService.removeItem(1L, "not-in-cart"));
    }
}
