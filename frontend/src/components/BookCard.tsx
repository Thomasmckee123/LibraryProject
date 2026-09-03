import { Link } from "react-router-dom";
import type { Book } from "../types";
import { formatPrice } from "../api/money";
import StockBadge from "./StockBadge";

interface BookCardProps {
  book: Book;
}

export default function BookCard({ book }: BookCardProps) {
  return (
    <Link
      to={`/books/${encodeURIComponent(book.isbn)}`}
      className="group flex flex-col gap-1 rounded-sm border border-rule bg-surface p-5 transition-all hover:border-gilt hover:shadow-[0_2px_12px_-4px_rgba(0,0,0,0.18)]"
    >
      <h2 className="text-lg font-semibold text-ink transition-colors group-hover:text-accent">{book.title}</h2>
      <p className="font-sans text-sm text-muted">{book.author}</p>
      <div className="mt-4 flex items-center justify-between">
        <span className="tabular text-lg font-semibold text-ink">
          {formatPrice(book.price)}
        </span>
        <StockBadge stock={book.stock} />
      </div>
    </Link>
  );
}
