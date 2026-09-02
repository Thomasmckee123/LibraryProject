# LibraryProject

A small Java library-tracking exercise built with Maven and JUnit 5.

## Model

| Class | Role |
| --- | --- |
| `Book` | Title, author, ISBN, borrow state. Equality is by ISBN. |
| `Member` | A borrower holding up to `Member.MAX_BORROWED` (3) books. |
| `Library` | Owns the catalogue and is the only place a book moves between the shelf and a member. |

`Library` is the single point of mutation for lending, so a `Book`'s borrow flag
and a `Member`'s borrowed list can never disagree. `Member.addBorrowed` /
`removeBorrowed` are package-private for that reason — go through
`Library.borrow` / `Library.returnBook`.

### Library API

```java
boolean        add(Book book);                        // false on null or duplicate ISBN
boolean        borrow(String isbn, Member member);    // false if unknown, out, or member at limit
boolean        returnBook(String isbn, Member member);// false if unknown or not held by that member
List<Book>     findByAuthor(String author);           // case-insensitive
Optional<Book> findByIsbn(String isbn);
List<Book>     availableBooks();
int            availableBookCount();
List<Book>     getBooks();
```

## Layout

```
library-tracker/
  pom.xml
  src/main/java/com/example/Book.java
  src/main/java/com/example/Member.java
  src/main/java/com/example/Library.java
  src/test/java/com/example/BookTest.java
  src/test/java/com/example/MemberTest.java
  src/test/java/com/example/LibraryTest.java
```

## Requirements

- JDK 21 or newer
- Maven 3.9+

## Build and test

```bash
cd library-tracker
mvn test
```
