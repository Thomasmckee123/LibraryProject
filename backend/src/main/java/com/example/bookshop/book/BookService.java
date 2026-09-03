package com.example.bookshop.book;

import com.example.bookshop.config.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Business logic for browsing the catalogue. Controllers stay thin and
 * delegate here - see backend/CLAUDE.md.
 */
@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * Lists books, optionally filtered by exactly one of {@code author} or
     * {@code q}. Passing both is a client error.
     */
    public List<Book> list(String author, String q) {
        boolean hasAuthor = author != null && !author.isBlank();
        boolean hasQuery = q != null && !q.isBlank();

        if (hasAuthor && hasQuery) {
            throw new IllegalArgumentException("author and q are mutually exclusive");
        }
        if (hasAuthor) {
            return bookRepository.findByAuthorIgnoreCase(author);
        }
        if (hasQuery) {
            return bookRepository.search(q);
        }
        return bookRepository.findAll();
    }

    public Book findByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> NotFoundException.book(isbn));
    }
}
