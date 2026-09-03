package com.example.bookshop.book;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only catalogue browsing. Thin by design: parse the request, call
 * {@link BookService}, map the result to a DTO - see backend/CLAUDE.md.
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<BookResponse> list(@RequestParam(required = false) String author,
                                    @RequestParam(required = false) String q) {
        return bookService.list(author, q).stream()
                .map(BookResponse::from)
                .toList();
    }

    @GetMapping("/{isbn}")
    public BookResponse getByIsbn(@PathVariable String isbn) {
        return BookResponse.from(bookService.findByIsbn(isbn));
    }
}
