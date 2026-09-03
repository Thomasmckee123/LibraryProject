package com.example.bookshop.cart;

import com.example.bookshop.book.Book;
import com.example.bookshop.book.BookRepository;
import com.example.bookshop.config.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Reads and mutates carts.
 *
 * <p>Adding a book to a cart reserves nothing: stock is only decremented at
 * checkout (see the order package), so two customers may hold the last copy
 * of a book at once. This service never calls {@link Book#reduceStock(int)}.
 * A book with zero stock is refused as a courtesy - reasonable UX, not a
 * reservation - but that check never mutates the book.
 *
 * <p>Every method is transactional because {@code Cart.items} is a lazy
 * collection and {@code open-in-view} is off: the Hibernate session must
 * still be open when a Cart is mapped to its DTO.
 */
@Service
public class CartService {

    private final CartRepository cartRepository;
    private final BookRepository bookRepository;

    public CartService(CartRepository cartRepository, BookRepository bookRepository) {
        this.cartRepository = cartRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Long cartId) {
        return toResponse(findCart(cartId));
    }

    /**
     * Sets a line to an absolute quantity.
     *
     * <p>{@link #addItem} only adds, so a quantity stepper moving downwards has
     * no positive delta it can send. Like addItem, this never touches stock.
     */
    @Transactional
    public CartResponse setQuantity(Long cartId, String isbn, SetQuantityRequest request) {
        Cart cart = findCart(cartId);
        CartItem item = cart.findItem(isbn)
                .orElseThrow(() -> NotFoundException.book(isbn));
        item.setQuantity(request.quantity());
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
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

    @Transactional
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
