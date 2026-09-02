package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LibraryTest {

    private Library library;
    private Member ada;
    private Book dune;
    private Book messiah;
    private Book emma;

    @BeforeEach
    void setUp() {
        library = new Library();
        ada = new Member("M1", "Ada");
        dune = new Book("Dune", "Frank Herbert", "111");
        messiah = new Book("Dune Messiah", "Frank Herbert", "222");
        emma = new Book("Emma", "Jane Austen", "333");
        library.add(dune);
        library.add(messiah);
        library.add(emma);
    }

    @Test
    void addRejectsNullAndDuplicateIsbn() {
        assertFalse(library.add(null));
        assertFalse(library.add(new Book("Dune (reprint)", "Frank Herbert", "111")));
        assertEquals(3, library.getBooks().size());
    }

    @Test
    void catalogueIsUnmodifiable() {
        assertThrows(UnsupportedOperationException.class,
                () -> library.getBooks().add(new Book("X", "Y", "999")));
    }

    @Test
    void findByAuthorIsCaseInsensitive() {
        List<Book> herbert = library.findByAuthor("frank HERBERT");
        assertEquals(2, herbert.size());
        assertTrue(herbert.containsAll(List.of(dune, messiah)));
    }

    @Test
    void findByAuthorHandlesMissesAndNull() {
        assertTrue(library.findByAuthor("Nobody").isEmpty());
        assertTrue(library.findByAuthor(null).isEmpty());
    }

    @Test
    void findByIsbnLocatesAndMisses() {
        assertEquals(dune, library.findByIsbn("111").orElseThrow());
        assertTrue(library.findByIsbn("nope").isEmpty());
        assertTrue(library.findByIsbn(null).isEmpty());
    }

    @Test
    void borrowMovesBookOffTheShelf() {
        assertTrue(library.borrow("111", ada));

        assertTrue(dune.isBorrowed());
        assertTrue(ada.hasBorrowed(dune));
        assertEquals(2, library.availableBookCount());
        assertFalse(library.availableBooks().contains(dune));
    }

    @Test
    void borrowFailsForUnknownIsbnOrNullMember() {
        assertFalse(library.borrow("does-not-exist", ada));
        assertFalse(library.borrow("111", null));
        assertEquals(3, library.availableBookCount());
    }

    @Test
    void aBookCannotBeBorrowedTwice() {
        Member bob = new Member("M2", "Bob");
        assertTrue(library.borrow("111", ada));

        assertFalse(library.borrow("111", bob));
        assertEquals(0, bob.getBorrowedCount());
        assertTrue(ada.hasBorrowed(dune));
    }

    @Test
    void borrowRespectsMemberLimitAndLeavesBookOnShelf() {
        for (int i = 0; i < Member.MAX_BORROWED; i++) {
            Book filler = new Book("Filler " + i, "Author", "filler-" + i);
            library.add(filler);
            assertTrue(library.borrow(filler.getIsbn(), ada));
        }

        assertFalse(library.borrow("111", ada));
        assertFalse(dune.isBorrowed());
        assertTrue(library.availableBooks().contains(dune));
    }

    @Test
    void returnPutsTheBookBack() {
        library.borrow("111", ada);

        assertTrue(library.returnBook("111", ada));
        assertFalse(dune.isBorrowed());
        assertFalse(ada.hasBorrowed(dune));
        assertEquals(3, library.availableBookCount());
    }

    @Test
    void returnFailsWhenTheMemberIsNotHoldingTheBook() {
        Member bob = new Member("M2", "Bob");
        library.borrow("111", ada);

        assertFalse(library.returnBook("111", bob));
        assertFalse(library.returnBook("does-not-exist", ada));
        assertTrue(dune.isBorrowed(), "a failed return must not free the book");
        assertTrue(ada.hasBorrowed(dune));
    }

    @Test
    void availableBooksStartsAsTheWholeCatalogue() {
        assertEquals(3, library.availableBookCount());
        assertTrue(library.availableBooks().containsAll(List.of(dune, messiah, emma)));
    }
}
