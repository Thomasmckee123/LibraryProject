import { Link, useParams } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { getBook } from "../api/books";
import { addItem } from "../api/cart";
import { ApiClientError } from "../api/client";
import { formatPrice } from "../api/money";
import StockBadge from "../components/StockBadge";
import styles from "./BookDetail.module.css";

/** One book, at "/books/:isbn": title, author, price, stock, add to cart. */
// No auth yet, so there is one shared cart. TODO: replace with a session.
const CART_ID = 1;

export default function BookDetail() {
  const { isbn } = useParams<{ isbn: string }>();
  const queryClient = useQueryClient();

  const addToCart = useMutation({
    mutationFn: () => addItem(CART_ID, isbn!, 1),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["cart", CART_ID] }),
  });

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
        disabled={book.stock === 0 || addToCart.isPending}
        onClick={() => addToCart.mutate()}
      >
        {addToCart.isPending ? "Adding..." : "Add to cart"}
      </button>

      {addToCart.isSuccess && (
        <p className={styles.added} role="status">
          Added to your cart. <Link to="/cart">View cart</Link>
        </p>
      )}
      {addToCart.isError && (
        <p className={styles.addError} role="alert">
          Could not add that to your cart. Please try again.
        </p>
      )}
    </section>
  );
}
