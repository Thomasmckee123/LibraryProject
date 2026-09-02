package com.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemberTest {

    @Test
    void newMemberHoldsNothing() {
        Member member = new Member("M1", "Ada");
        assertEquals(0, member.getBorrowedCount());
        assertTrue(member.canBorrow());
    }

    @Test
    void blankIdentityIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Member("", "Ada"));
        assertThrows(IllegalArgumentException.class, () -> new Member("M1", " "));
    }

    @Test
    void borrowedListIsUnmodifiable() {
        Member member = new Member("M1", "Ada");
        assertThrows(UnsupportedOperationException.class,
                () -> member.getBorrowedBooks().add(new Book("Dune", "Herbert", "111")));
    }

    @Test
    void cannotHoldMoreThanTheLimit() {
        Member member = new Member("M1", "Ada");
        for (int i = 0; i < Member.MAX_BORROWED; i++) {
            assertTrue(member.addBorrowed(new Book("Book " + i, "Author", "isbn-" + i)));
        }
        assertFalse(member.canBorrow());
        assertFalse(member.addBorrowed(new Book("One too many", "Author", "isbn-x")));
        assertEquals(Member.MAX_BORROWED, member.getBorrowedCount());
    }

    @Test
    void returningFreesUpCapacity() {
        Member member = new Member("M1", "Ada");
        Book book = new Book("Dune", "Herbert", "111");
        member.addBorrowed(book);

        assertTrue(member.hasBorrowed(book));
        assertTrue(member.removeBorrowed(book));
        assertFalse(member.hasBorrowed(book));
        assertFalse(member.removeBorrowed(book));
    }
}
