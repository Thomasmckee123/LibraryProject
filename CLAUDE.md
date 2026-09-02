# LibraryProject

Java 21 / Maven library-tracking exercise. Source lives in `library-tracker/`.

## Build

```bash
cd library-tracker && mvn -o -q test
```

Runs offline in ~1.2s. Use `-o` unless dependencies changed.

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

## Hooks

Configured in `.claude/settings.json`, scripts in `.claude/hooks/`:

| Event | Script | Effect |
|---|---|---|
| `PostToolUse` (Write/Edit) | `test-on-java-edit.sh` | Runs the suite on `.java` changes; exit 2 returns failures |
| `PreToolUse` (Bash) | `guard-main-commit.sh` | Blocks `git commit` while on `main` |
| `Stop` | `stop-status.sh` | Advisory note on unpushed work or a missing PR |

Hooks are shell commands the harness runs — review them before trusting a
clone. `/hooks` lists and disables them.
