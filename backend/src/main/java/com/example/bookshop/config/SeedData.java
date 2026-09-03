package com.example.bookshop.config;

import com.example.bookshop.book.Book;
import com.example.bookshop.book.BookRepository;
import com.example.bookshop.customer.Customer;
import com.example.bookshop.customer.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

/**
 * H2 is in-memory and drops on shutdown, so the catalogue is rebuilt on every
 * start. Stock levels vary deliberately - including one out-of-stock title, so
 * the "unavailable" path is visible without editing data.
 */
@Configuration
public class SeedData {

    @Bean
    CommandLineRunner seed(BookRepository books, CustomerRepository customers) {
        return args -> {
            if (books.count() > 0) {
                return;
            }

            books.saveAll(List.of(
                    new Book("9780441013593", "Dune", "Frank Herbert", new BigDecimal("9.99"), 12),
                    new Book("9780441013609", "Dune Messiah", "Frank Herbert", new BigDecimal("8.99"), 7),
                    new Book("9780141439587", "Emma", "Jane Austen", new BigDecimal("6.50"), 20),
                    new Book("9780141439518", "Pride and Prejudice", "Jane Austen", new BigDecimal("6.50"), 15),
                    new Book("9780571056866", "Lord of the Flies", "William Golding", new BigDecimal("7.25"), 4),
                    new Book("9780451524935", "Nineteen Eighty-Four", "George Orwell", new BigDecimal("8.15"), 30),
                    new Book("9780452284241", "Animal Farm", "George Orwell", new BigDecimal("5.99"), 0),
                    new Book("9780618640157", "The Lord of the Rings", "J. R. R. Tolkien", new BigDecimal("24.99"), 3),
                    new Book("9780547928227", "The Hobbit", "J. R. R. Tolkien", new BigDecimal("10.50"), 18),
                    new Book("9780099800200", "Slaughterhouse-Five", "Kurt Vonnegut", new BigDecimal("8.75"), 9),
                    new Book("9780156012195", "The Little Prince", "Antoine de Saint-Exupery", new BigDecimal("7.99"), 25),
                    new Book("9781400079988", "Anna Karenina", "Leo Tolstoy", new BigDecimal("12.00"), 6)));

            customers.save(new Customer("Thomas McKee", "thomas@example.com"));
        };
    }
}
