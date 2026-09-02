package com.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Stage 3: add, borrow, returnBook, findByAuthor, availableBooks.
 *
 * <p>The library owns the catalogue of {@link Book}s and is the only place that
 * moves a book between a shelf and a {@link Member}, so a book's borrow flag and
 * a member's borrowed list can never drift apart.
 */
public class Library {

    private final List<Book> books = new ArrayList<>();

    /**
     * Adds a book to the catalogue.
     *
     * @return false if the book is null or an entry with the same ISBN already exists
     */
    public boolean add(Book book) {
        if (book == null || books.contains(book)) {
            return false;
        }
        return books.add(book);
    }

    /** The full catalogue, borrowed or not, as an unmodifiable view. */
    public List<Book> getBooks() {
        return Collections.unmodifiableList(books);
    }

    public Optional<Book> findByIsbn(String isbn) {
        if (isbn == null) {
            return Optional.empty();
        }
        return books.stream()
                .filter(book -> isbn.equals(book.getIsbn()))
                .findFirst();
    }

    /** Every book by the given author, matched case-insensitively. */
    public List<Book> findByAuthor(String author) {
        if (author == null) {
            return List.of();
        }
        return books.stream()
                .filter(book -> author.equalsIgnoreCase(book.getAuthor()))
                .toList();
    }

    /** Every book currently on the shelf. */
    public List<Book> availableBooks() {
        return books.stream()
                .filter(book -> !book.isBorrowed())
                .toList();
    }

    public int availableBookCount() {
        return availableBooks().size();
    }

    /**
     * Lends the book with the given ISBN to a member.
     *
     * @return false if the ISBN is unknown, the book is already out, or the
     *         member is at their borrowing limit
     */
    public boolean borrow(String isbn, Member member) {
        if (member == null) {
            return false;
        }
        Optional<Book> found = findByIsbn(isbn);
        if (found.isEmpty()) {
            return false;
        }
        Book book = found.get();
        if (book.isBorrowed() || !member.addBorrowed(book)) {
            return false;
        }
        book.setBorrowed(true);
        return true;
    }

    /**
     * Takes the book with the given ISBN back from a member.
     *
     * @return false if the ISBN is unknown or that member is not holding it
     */
    public boolean returnBook(String isbn, Member member) {
        if (member == null) {
            return false;
        }
        Optional<Book> found = findByIsbn(isbn);
        if (found.isEmpty()) {
            return false;
        }
        Book book = found.get();
        if (!member.removeBorrowed(book)) {
            return false;
        }
        book.setBorrowed(false);
        return true;
    }
}
