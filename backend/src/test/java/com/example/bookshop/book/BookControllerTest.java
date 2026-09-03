package com.example.bookshop.book;

import com.example.bookshop.config.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @Test
    void listReturnsAllBooksAsJson() throws Exception {
        Book dune = new Book("111", "Dune", "Frank Herbert", new BigDecimal("12.50"), 4);
        when(bookService.list(null, null)).thenReturn(List.of(dune));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isbn").value("111"))
                .andExpect(jsonPath("$[0].title").value("Dune"))
                .andExpect(jsonPath("$[0].author").value("Frank Herbert"))
                .andExpect(jsonPath("$[0].price").value(12.50))
                .andExpect(jsonPath("$[0].stock").value(4));
    }

    @Test
    void listPassesAuthorFilterThrough() throws Exception {
        when(bookService.list("Tolkien", null)).thenReturn(List.of());

        mockMvc.perform(get("/api/books").param("author", "Tolkien"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(bookService).list("Tolkien", null);
    }

    @Test
    void listPassesSearchTermThrough() throws Exception {
        when(bookService.list(null, "hobbit")).thenReturn(List.of());

        mockMvc.perform(get("/api/books").param("q", "hobbit"))
                .andExpect(status().isOk());

        verify(bookService).list(null, "hobbit");
    }

    @Test
    void bothAuthorAndQueryIsRejectedByService() throws Exception {
        when(bookService.list("Tolkien", "hobbit"))
                .thenThrow(new IllegalArgumentException("author and q are mutually exclusive"));

        mockMvc.perform(get("/api/books").param("author", "Tolkien").param("q", "hobbit"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByIsbnReturnsTheBook() throws Exception {
        Book dune = new Book("111", "Dune", "Frank Herbert", new BigDecimal("12.50"), 4);
        when(bookService.findByIsbn("111")).thenReturn(dune);

        mockMvc.perform(get("/api/books/111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").value("111"))
                .andExpect(jsonPath("$.title").value("Dune"));
    }

    @Test
    void getByIsbnReturns404ForUnknownIsbn() throws Exception {
        when(bookService.findByIsbn("does-not-exist")).thenThrow(NotFoundException.book("does-not-exist"));

        mockMvc.perform(get("/api/books/does-not-exist"))
                .andExpect(status().isNotFound());
    }
}
