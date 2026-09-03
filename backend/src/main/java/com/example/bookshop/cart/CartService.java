package com.example.bookshop.cart;

import com.example.bookshop.book.Book;
import com.example.bookshop.book.BookRepository;
import com.example.bookshop.config.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Reads and mutates carts.
 *
 * <p>Adding a book to a cart reserves nothing: stock is only decremented at
 * checkout (see the order package), so two customers may hold the last copy
 * of a book at once. This service never calls {@link Book#reduceStock(int)}.
 * A book with zero stock is refused as a courtesy - reasonable UX, not a
 * reservation - but that check never mutates the book.
 */
@Service
public class CartService {

    private final CartRepository cartRepository;
    private final BookRepository bookRepository;

    public CartService(CartRepository cartRepository, BookRepository bookRepository) {
        this.cartRepository = cartRepository;
        this.bookRepository = bookRepository;
    }

    public CartResponse getCart(Long cartId) {
        return toResponse(findCart(cartId));
    }

    public CartResponse addItem(Long cartId, AddItemRequest request) {
        Cart cart = findCart(cartId);
        Book book = bookRepository.findByIsbn(request.isbn())
                .orElseThrow(() -> NotFoundException.book(request.isbn()));
        if (!book.isInStock()) {
            throw new IllegalArgumentException(
                    "No copies of " + book.getIsbn() + " are currently in stock");
        }

        cart.addItem(book, request.quantity());
        cartRepository.save(cart);
        return toResponse(cart);
    }

    public CartResponse removeItem(Long cartId, String isbn) {
        Cart cart = findCart(cartId);
        if (!cart.removeItem(isbn)) {
            throw NotFoundException.book(isbn);
        }

        cartRepository.save(cart);
        return toResponse(cart);
    }

    private Cart findCart(Long cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> NotFoundException.cart(cartId));
    }

    private CartResponse toResponse(Cart cart) {
        List<CartLineResponse> lines = cart.getItems().stream()
                .map(item -> new CartLineResponse(
                        item.getBook().getIsbn(),
                        item.getBook().getTitle(),
                        item.getBook().getAuthor(),
                        item.getBook().getPrice(),
                        item.getQuantity(),
                        item.lineTotal()))
                .toList();
        return new CartResponse(cart.getId(), lines, cart.total());
    }
}
