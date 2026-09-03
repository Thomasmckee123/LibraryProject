# LibraryProject

An online bookshop. Browse a catalogue, add books to a cart, check out.
React frontend talking to a Spring Boot API.

```
frontend/          React + Vite      see frontend/CLAUDE.md
backend/           Spring Boot 3     see backend/CLAUDE.md
library-tracker/   legacy — read "Current state" before touching
```

## Current state

Nothing in `frontend/` or `backend/` is built yet. Both hold a CLAUDE.md and
no code. Don't describe this project as working.

`library-tracker/` is a finished lending exercise — `Book`, `Member`,
`Library`, 22 passing tests. **That direction has been retired.** This is a
shop now: books are bought, not borrowed. Lending, loans, and
`MAX_BORROWED` are all out of scope.

The `Book` model is the one piece worth carrying across — title, author,
isbn, ISBN-based equality, validated constructor. It gains `price` and
`stock` and loses `isBorrowed`. Everything else in `library-tracker/` gets
deleted when `backend/` lands; leave it alone until then rather than
half-migrating it.

## Domain

| Entity | Holds |
|---|---|
| `Book` | isbn, title, author, price, stock — the product |
| `Customer` | id, name, email |
| `Cart` / `CartItem` | one open cart per customer, quantity per book |
| `Order` / `OrderLine` | a checked-out cart, frozen with prices as they were |

Two rules that shape everything:

- **An order line stores the price it was bought at.** Never join back to
  `Book` for a historical price — changing a book's price must not rewrite
  past orders.
- **Stock decrements at checkout, not when added to a cart.** A book in
  someone's cart is still available to everyone else. Checkout is where
  availability is decided, and it must reject an order it cannot fill.

## Payments are simulated

Checkout produces an `Order` with a `PAID` status and returns a fake
confirmation reference. There is no payment provider, no card handling, no
real money. Do not add a live payment integration without Thomas asking for
it explicitly — that needs real keys and is his decision, not a detail to
fill in.

## Working style

Thomas is learning — he is using this project to pick up Java, and now
Spring and React alongside it. Favour the conventional, well-documented way
of doing a thing over the clever one, and say why a pattern exists rather
than just applying it. When a task looks like it is meant as practice, offer
the scaffold-and-fill option instead of writing it all.

## Branching

Work reaches `main` through pull requests, never a direct commit.

1. `git checkout -b feature/<short-name>`
2. Commit, then `git push -u origin <branch>`
3. `gh pr create --fill`
4. Three review jobs run (correctness, tests, design)
5. Address findings, merge

A `PreToolUse` hook blocks commits on `main`, so step 1 is enforced rather
than remembered.

## Hooks

Three run automatically: tests after any `.java` edit, a block on commits to
`main`, and an end-of-turn status note. Config in `.claude/settings.json`,
scripts in `.claude/hooks/`.

They are shell commands the harness executes with full privileges — read
them before trusting them in a clone. `/hooks` lists and disables them.

The test hook currently runs Maven against `library-tracker/`. It needs
repointing at `backend/` when that exists.
