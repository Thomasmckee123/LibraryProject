import { useParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { getBook } from "../api/books";
import { ApiClientError } from "../api/client";
import { formatPrice } from "../api/money";
import StockBadge from "../components/StockBadge";
import styles from "./BookDetail.module.css";

/** One book, at "/books/:isbn": title, author, price, stock, add to cart. */
export default function BookDetail() {
  const { isbn } = useParams<{ isbn: string }>();

  const {
    data: book,
    isPending,
    isError,
    error,
  } = useQuery({
    queryKey: ["book", isbn],
    queryFn: () => getBook(isbn!),
    enabled: Boolean(isbn),
  });

  if (!isbn) {
    return (
      <section className={styles.page}>
        <p className={`${styles.status} ${styles["status--error"]}`}>No book was specified.</p>
      </section>
    );
  }

  if (isPending) {
    return (
      <section className={styles.page}>
        <p className={styles.status}>Loading book...</p>
      </section>
    );
  }

  if (isError) {
    const notFound = error instanceof ApiClientError && error.status === 404;
    return (
      <section className={styles.page}>
        <p className={`${styles.status} ${styles["status--error"]}`}>
          {notFound
            ? `No book found for ISBN ${isbn}.`
            : `Could not load this book: ${error instanceof Error ? error.message : "unknown error"}`}
        </p>
      </section>
    );
  }

  return (
    <section className={styles.page}>
      <h1>{book.title}</h1>
      <p className={styles.author}>{book.author}</p>
      <div className={styles.priceRow}>
        <span className={`price ${styles.price}`}>{formatPrice(book.price)}</span>
        <StockBadge stock={book.stock} />
      </div>
      <button
        type="button"
        disabled={book.stock === 0}
        onClick={() => {
          // TODO(cart-agent): wire this up once api/cart.ts exists. Owned by
          // the agent building the cart/checkout experience - not this one.
        }}
      >
        Add to cart
      </button>
    </section>
  );
}
