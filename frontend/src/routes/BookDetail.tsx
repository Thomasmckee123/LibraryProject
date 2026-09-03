import { Link, useParams } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { getBook } from "../api/books";
import { addItem } from "../api/cart";
import { ApiClientError } from "../api/client";
import { formatPrice } from "../api/money";
import StockBadge from "../components/StockBadge";

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
      <section className="flex flex-col gap-4">
        <p className="font-sans text-sm text-bad" role="alert">No book was specified.</p>
      </section>
    );
  }

  if (isPending) {
    return (
      <section className="flex flex-col gap-4">
        <p className="font-sans text-sm text-muted">Loading book...</p>
      </section>
    );
  }

  if (isError) {
    const notFound = error instanceof ApiClientError && error.status === 404;
    return (
      <section className="flex flex-col gap-4">
        <p className="font-sans text-sm text-bad" role="alert">
          {notFound
            ? `No book found for ISBN ${isbn}.`
            : `Could not load this book: ${error instanceof Error ? error.message : "unknown error"}`}
        </p>
      </section>
    );
  }

  return (
    <section className="flex flex-col gap-4">
      <h1 className="text-4xl font-semibold">{book.title}</h1>
      <p className="font-sans text-base text-muted">{book.author}</p>
      <div className="flex items-center gap-4">
        <span className="tabular text-2xl font-semibold text-ink">{formatPrice(book.price)}</span>
        <StockBadge stock={book.stock} />
      </div>
      <button
        type="button"
        className="w-fit rounded bg-accent px-5 py-2.5 font-sans text-sm text-surface transition-opacity hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-40"
        disabled={book.stock === 0 || addToCart.isPending}
        onClick={() => addToCart.mutate()}
      >
        {addToCart.isPending ? "Adding..." : "Add to cart"}
      </button>

      {addToCart.isSuccess && (
        <p className="font-sans text-sm text-good" role="status">
          Added to your cart. <Link to="/cart">View cart</Link>
        </p>
      )}
      {addToCart.isError && (
        <p className="font-sans text-sm text-bad" role="alert">
          Could not add that to your cart. Please try again.
        </p>
      )}
    </section>
  );
}
