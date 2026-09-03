import { Link } from "react-router-dom";
import type { Book } from "../types";
import { formatPrice } from "../api/money";
import StockBadge from "./StockBadge";
import styles from "./BookCard.module.css";

/** Cover-less card: title, author, price, stock, links to the detail route. */
export default function BookCard({ book }: { book: Book }) {
  return (
    <Link to={`/books/${encodeURIComponent(book.isbn)}`} className={styles.card}>
      <h2 className={styles.title}>{book.title}</h2>
      <p className={styles.author}>{book.author}</p>
      <div className={styles.footer}>
        <span className={`price ${styles.price}`}>{formatPrice(book.price)}</span>
        <StockBadge stock={book.stock} />
      </div>
    </Link>
  );
}
