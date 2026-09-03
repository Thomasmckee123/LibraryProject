import { useEffect, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { listBooks } from "../api/books";
import BookCard from "../components/BookCard";
import styles from "./Catalogue.module.css";

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
    <section className={styles.page}>
      <h1>Catalogue</h1>
      <input
        type="search"
        className={styles.search}
        placeholder="Search by title or author"
        value={searchText}
        onChange={(event) => setSearchText(event.target.value)}
        aria-label="Search books by title or author"
      />

      {isPending && <p className={styles.status}>Loading books...</p>}

      {isError && (
        <p className={`${styles.status} ${styles["status--error"]}`}>
          Could not load books: {error instanceof Error ? error.message : "unknown error"}
        </p>
      )}

      {!isPending && !isError && books.length === 0 && (
        <p className={styles.status}>No books match your search.</p>
      )}

      {!isPending && !isError && books.length > 0 && (
        <div className={styles.grid}>
          {books.map((book) => (
            <BookCard key={book.isbn} book={book} />
          ))}
        </div>
      )}
    </section>
  );
}
