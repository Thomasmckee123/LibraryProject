package com.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookTest {

    @Test
    void newBookIsNotBorrowedByDefault() {
        Book book = new Book("Dune", "Frank Herbert", "978-0441013593");
        assertFalse(book.isBorrowed());
    }

    @Test
    void booksAreEqualWhenIsbnMatches() {
        Book one = new Book("Dune", "Frank Herbert", "111");
        Book two = new Book("Dune (reprint)", "F. Herbert", "111");
        assertEquals(one, two);
        assertEquals(one.hashCode(), two.hashCode());
    }

    @Test
    void booksDifferWhenIsbnDiffers() {
        assertNotEquals(new Book("Dune", "Frank Herbert", "111"),
                new Book("Dune", "Frank Herbert", "222"));
    }

    @Test
    void blankIsbnIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Book("Dune", "Frank Herbert", "  "));
        assertThrows(IllegalArgumentException.class,
                () -> new Book("Dune", "Frank Herbert", null));
    }

    @Test
    void settersUpdateMutableFields() {
        Book book = new Book("Dune", "Frank Herbert", "111");
        book.setTitle("Dune Messiah");
        book.setAuthor("Herbert, Frank");
        book.setBorrowed(true);

        assertEquals("Dune Messiah", book.getTitle());
        assertEquals("Herbert, Frank", book.getAuthor());
        assertTrue(book.isBorrowed());
    }
}
