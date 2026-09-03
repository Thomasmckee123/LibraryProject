# backend

Spring Boot 3 REST API for the bookshop. Java 21, Maven, JPA, H2.

**Nothing here yet.** This file is the plan, not a description.

## Stack

| Piece | Choice | Why |
|---|---|---|
| Framework | Spring Boot 3 | The default for a Java API; everything you'll read online assumes it |
| Persistence | Spring Data JPA | Repository interfaces, no hand-written SQL to start |
| Database | H2, in-memory | Zero setup, resets each run. Swap for Postgres when data needs to survive |
| Tests | JUnit 5 + MockMvc | Same JUnit you already know, plus HTTP-level tests |
| Build | Maven | Matches what's already here |

Target Java 21 even though the installed JDK is 25 — same deliberate gap as
the old module.

## Layout

```
src/main/java/com/example/bookshop/
  BookshopApplication.java
  book/        Book, BookRepository, BookController, BookService
  cart/        Cart, CartItem, CartRepository, CartController, CartService
  order/       Order, OrderLine, OrderRepository, OrderController, CheckoutService
  customer/    Customer, CustomerRepository
  config/      SeedData, CORS
```

Package **by feature**, not by layer. `book/` holds the entity, repository,
service, and controller together. A `controllers/` package holding every
controller in the app looks tidy and means every change touches four
directories.

## Endpoints

```
GET    /api/books                 list, supports ?author= and ?q=
GET    /api/books/{isbn}          one book
POST   /api/carts/{id}/items      add { isbn, quantity }
DELETE /api/carts/{id}/items/{isbn}
GET    /api/carts/{id}            cart with line totals
POST   /api/carts/{id}/checkout   → Order, or 409 if stock is short
GET    /api/orders/{id}           one order
```

Money is `BigDecimal`, never `double` — binary floating point cannot
represent 0.10, and prices that don't add up correctly are the classic
version of that bug. Persist as `DECIMAL(10,2)`.

## Conventions

**Controllers stay thin.** Parse the request, call a service, map the
result. No business rules in a controller — they can't be unit-tested
without spinning up HTTP.

**Entities don't leave the service layer.** Controllers return DTOs
(`BookResponse`, `CartResponse`). Returning a JPA entity straight out of a
controller leaks the schema into the API and drags lazy-loading problems
into JSON serialization.

**Errors are status codes, not 200-with-a-message.** Unknown isbn → 404.
Insufficient stock → 409. Malformed body → 400. Use
`@ControllerAdvice` for one consistent error shape.

**Checkout is the one place that must be transactional.** Read stock,
verify, decrement, write the order — `@Transactional`, all or nothing. A
half-completed checkout that takes stock without creating an order is the
worst bug this app can have.

## Carrying `Book` over

`library-tracker/src/main/java/com/example/Book.java` is worth reusing:
ISBN-based `equals`/`hashCode`, a validated constructor, `final` isbn.

Keep all of that. Add `price` (`BigDecimal`) and `stock` (`int`). Drop
`isBorrowed`. Adding `@Entity` needs a protected no-arg constructor for JPA
— that's a JPA requirement, not a reason to drop the validation from the
real one.

## Tests

- **Unit** — services with mocked repositories. Fast, most of the coverage.
- **Slice** — `@WebMvcTest` for controllers, `@DataJpaTest` for queries.
- **Integration** — `@SpringBootTest` for checkout end to end, since that's
  the path where a transaction bug would hide.

Assert behaviour through the API, not internals. A test that would still
pass with the implementation deleted isn't a test — the CI reviewer flags
those.
