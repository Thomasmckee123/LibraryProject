# LibraryProject

A staged Java exercise for learning the language. Source lives in
`library-tracker/`.

The stages build on each other, and the numbering is in the source comments:

| Stage | Class | State |
|---|---|---|
| 1 | `Book` | done — title, author, isbn, borrow flag |
| 2 | `Member` | done — a borrower with a holding limit |
| 3 | `Library` | done — add, borrow, returnBook, findByAuthor, availableBooks |

No stage 4 has been mentioned. If new work doesn't fit an existing stage, ask
where it belongs rather than inventing one.

## Working style

Thomas is learning Java, so how much I write is a real choice, not a detail.
On stage 3 he asked for a full implementation rather than a scaffold to fill
in — take that as the default, but it was one decision about one stage, so
offer the alternative when a task looks like it's meant as practice.

## Build

```bash
cd library-tracker && mvn -o -q test
```

Runs offline in ~1.2s. Use `-o` unless dependencies changed.

The POM targets Java 21 while the installed JDK is 25. That gap is
deliberate — don't "fix" it by bumping `maven.compiler.source/target`.

## Branching

Work lands on `main` through pull requests, never by direct commit.

1. `git checkout -b feature/<short-name>` before starting a unit of work
2. Commit there, `git push -u origin <branch>`
3. `gh pr create --fill`
4. Three review jobs run on the PR (correctness, tests, design)
5. Address findings, then merge

A `PreToolUse` hook blocks commits made on `main`, so step 1 is enforced
rather than remembered.

## Design invariant

`Library` is the only thing that lends. `Member.addBorrowed` and
`removeBorrowed` are package-private so a `Book`'s borrow flag and a
`Member`'s list cannot drift apart. Do not add a public path that mutates
either one directly.

## Decisions the code doesn't explain

Each of these looks like an oversight and isn't. Read before "fixing" one.

- **`Member.MAX_BORROWED = 3`** is invented, not from a spec. No rule says a
  library member may hold three books — the constant exists so the limit is
  changeable in one place. Change it freely; just know there's nothing
  authoritative behind the number.
- **`Book.isbn` is `final` and there is no `setIsbn`.** `equals` and
  `hashCode` are ISBN-based, so mutating it while a `Book` sits in a list or
  set silently corrupts lookups. The setter was removed on purpose.
- **`Book` setters return `void`.** They originally returned the value just
  assigned; nothing consumed it.
- **`findByAuthor` is case-insensitive**, so `"frank HERBERT"` matches
  `"Frank Herbert"`. A deliberate call, not a stray `equalsIgnoreCase`.
- **Lending methods return `boolean`, they don't throw.** "Already borrowed"
  and "at your limit" are expected outcomes of a normal request, not errors.
  Keep new lending paths consistent with that.

## Tests

JUnit 5, one test class per production class. 22 tests, all passing.

Tests assert observable behavior through the public API — they don't reach
into internals or assert that a method merely ran. Keep new tests to that
standard; the CI reviewer flags tests that would still pass with the
implementation deleted.

## Hooks

Three hooks run automatically: tests after any `.java` edit, a block on
commits to `main`, and an end-of-turn status note. Config is in
`.claude/settings.json`, scripts in `.claude/hooks/`.

They are shell commands the harness executes with full privileges — read
them before trusting them in a clone. `/hooks` lists and disables them.
