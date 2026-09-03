package com.example.bookshop.book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    List<Book> findByAuthorIgnoreCase(String author);

    @Query("""
            select b from Book b
            where lower(b.title) like lower(concat('%', :term, '%'))
               or lower(b.author) like lower(concat('%', :term, '%'))
            """)
    List<Book> search(String term);
}
