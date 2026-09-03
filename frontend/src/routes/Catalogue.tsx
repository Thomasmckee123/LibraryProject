import { useEffect, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { listBooks } from "../api/books";
import BookCard from "../components/BookCard";

const DEBOUNCE_MS = 300;

/** The shop front at "/": search box plus a grid of books. */
export default function Catalogue() {
  const [searchText, setSearchText] = useState("");
  const [query, setQuery] = useState("");

  // Debounce typing so we don't fire a request per keystroke.
  useEffect(() => {
    const timeout = setTimeout(() => setQuery(searchText.trim()), DEBOUNCE_MS);
    return () => clearTimeout(timeout);
  }, [searchText]);

  const {
    data: books,
    isPending,
    isError,
    error,
  } = useQuery({
    queryKey: ["books", { q: query }],
    queryFn: () => listBooks(query ? { q: query } : undefined),
  });

  return (
    <section className="flex flex-col gap-6">
      <h1 className="text-4xl font-semibold">Catalogue</h1>
      <input
        type="search"
        className="w-full max-w-sm rounded border border-rule bg-surface px-4 py-2.5 font-sans text-sm text-ink placeholder:text-muted focus:border-accent focus:outline-none"
        placeholder="Search by title or author"
        value={searchText}
        onChange={(event) => setSearchText(event.target.value)}
        aria-label="Search books by title or author"
      />

      {isPending && <p className="font-sans text-sm text-muted">Loading books...</p>}

      {isError && (
        <p className="font-sans text-sm text-bad" role="alert">
          Could not load books: {error instanceof Error ? error.message : "unknown error"}
        </p>
      )}

      {!isPending && !isError && books.length === 0 && (
        <p className="font-sans text-sm text-muted">No books match your search.</p>
      )}

      {!isPending && !isError && books.length > 0 && (
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {books.map((book) => (
            <BookCard key={book.isbn} book={book} />
          ))}
        </div>
      )}
    </section>
  );
}
