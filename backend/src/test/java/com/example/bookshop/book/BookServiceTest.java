package com.example.bookshop.book;

import com.example.bookshop.config.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    private BookService bookService;

    private Book dune;
    private Book hobbit;

    @BeforeEach
    void setUp() {
        bookService = new BookService(bookRepository);
        dune = new Book("111", "Dune", "Frank Herbert", new BigDecimal("12.50"), 4);
        hobbit = new Book("222", "The Hobbit", "J.R.R. Tolkien", new BigDecimal("9.99"), 0);
    }

    @Test
    void listReturnsEveryBookWhenNoFilterGiven() {
        when(bookRepository.findAll()).thenReturn(List.of(dune, hobbit));

        List<Book> result = bookService.list(null, null);

        assertThat(result).containsExactly(dune, hobbit);
        verify(bookRepository, never()).findByAuthorIgnoreCase(org.mockito.ArgumentMatchers.anyString());
        verify(bookRepository, never()).search(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void listFiltersByAuthorCaseInsensitively() {
        when(bookRepository.findByAuthorIgnoreCase("frank herbert")).thenReturn(List.of(dune));

        List<Book> result = bookService.list("frank herbert", null);

        assertThat(result).containsExactly(dune);
        verify(bookRepository, never()).findAll();
    }

    @Test
    void listSearchesTitleOrAuthor() {
        when(bookRepository.search("hobbit")).thenReturn(List.of(hobbit));

        List<Book> result = bookService.list(null, "hobbit");

        assertThat(result).containsExactly(hobbit);
        verify(bookRepository, never()).findAll();
    }

    @Test
    void listRejectsBothAuthorAndQuery() {
        assertThatThrownBy(() -> bookService.list("Frank Herbert", "hobbit"))
                .isInstanceOf(IllegalArgumentException.class);

        verify(bookRepository, never()).findAll();
        verify(bookRepository, never()).findByAuthorIgnoreCase(org.mockito.ArgumentMatchers.anyString());
        verify(bookRepository, never()).search(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void findByIsbnReturnsTheMatchingBook() {
        when(bookRepository.findByIsbn("111")).thenReturn(Optional.of(dune));

        Book result = bookService.findByIsbn("111");

        assertThat(result).isEqualTo(dune);
        assertThat(result.getTitle()).isEqualTo("Dune");
    }

    @Test
    void findByIsbnThrowsNotFoundForUnknownIsbn() {
        when(bookRepository.findByIsbn("does-not-exist")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.findByIsbn("does-not-exist"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("does-not-exist");
    }
}
