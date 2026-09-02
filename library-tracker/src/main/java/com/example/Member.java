package com.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stage 2: a borrower who can hold up to {@link #MAX_BORROWED} books at a time.
 */
public class Member {

    /** Maximum number of books a member may hold at once. */
    public static final int MAX_BORROWED = 3;

    private final String memberId;
    private String name;
    private final List<Book> borrowedBooks = new ArrayList<>();

    public Member(String memberId, String name) {
        if (memberId == null || memberId.isBlank()) {
            throw new IllegalArgumentException("memberId must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        this.memberId = memberId;
        this.name = name;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        this.name = name;
    }

    /** Books currently held by this member, as an unmodifiable view. */
    public List<Book> getBorrowedBooks() {
        return Collections.unmodifiableList(borrowedBooks);
    }

    public int getBorrowedCount() {
        return borrowedBooks.size();
    }

    public boolean canBorrow() {
        return borrowedBooks.size() < MAX_BORROWED;
    }

    public boolean hasBorrowed(Book book) {
        return book != null && borrowedBooks.contains(book);
    }

    /**
     * Records that this member has taken the book.
     *
     * @return false if the member is at their limit or already holds the book
     */
    boolean addBorrowed(Book book) {
        if (book == null || !canBorrow() || borrowedBooks.contains(book)) {
            return false;
        }
        return borrowedBooks.add(book);
    }

    /**
     * Records that this member has given the book back.
     *
     * @return false if the member was not holding the book
     */
    boolean removeBorrowed(Book book) {
        return book != null && borrowedBooks.remove(book);
    }

    @Override
    public String toString() {
        return "Member{memberId='" + memberId + "', name='" + name
                + "', borrowed=" + borrowedBooks.size() + "}";
    }
}
