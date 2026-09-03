import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import type { Order } from "../types";
import { listOrders } from "../api/orders";
import { formatPrice } from "../api/money";
import EmptyState from "../components/EmptyState";

// TODO: there is no login/session yet, so this is always the seeded demo
// customer's history. Replace with the signed-in customer's id once accounts
// exist - see issue #17. Matches CART_ID in Cart.tsx, which has the same gap.
const CUSTOMER_ID = 1;

/** "3 books" / "1 book" - the count of physical copies, not of lines. */
function itemCount(order: Order): string {
  const books = order.lines.reduce((count, line) => count + line.quantity, 0);
  return `${books} ${books === 1 ? "book" : "books"}`;
}

/**
 * Order history at /orders.
 *
 * Every price on this page comes from the order's own frozen lines, never
 * from the current catalogue - that is the whole point of the page. It reads
 * the totals the server sends and formats them; it never sums anything
 * itself (see api/money.ts). The one number computed here is the item count,
 * which is not money.
 */
export default function Orders() {
  const ordersQuery = useQuery({
    queryKey: ["orders", CUSTOMER_ID],
    queryFn: () => listOrders(CUSTOMER_ID),
  });

  return (
    <section className="flex flex-col gap-6">
      <h1 className="text-4xl font-semibold">Your orders</h1>

      {ordersQuery.isPending && <p className="placeholder">Loading your orders…</p>}

      {ordersQuery.isError && (
        <p className="font-sans text-sm text-bad" role="alert">
          Couldn&rsquo;t load your orders: {ordersQuery.error.message}
        </p>
      )}

      {ordersQuery.isSuccess && ordersQuery.data.length === 0 && (
        <EmptyState
          message="You haven't placed any orders yet."
          action={<Link to="/">Browse the catalogue</Link>}
        />
      )}

      {ordersQuery.isSuccess && ordersQuery.data.length > 0 && (
        <ul className="flex flex-col gap-3">
          {ordersQuery.data.map((order) => (
            <li key={order.id}>
              <Link
                to={`/orders/${order.id}`}
                className="flex flex-wrap items-baseline justify-between gap-x-6 gap-y-2 rounded border border-rule bg-surface px-4 py-4 transition-colors hover:border-gilt"
              >
                <span className="flex flex-col gap-1">
                  <span className="tabular font-sans text-base font-semibold text-ink">
                    {order.reference}
                  </span>
                  <span className="font-sans text-xs text-muted">
                    {new Date(order.placedAt).toLocaleDateString("en-GB", {
                      day: "numeric",
                      month: "long",
                      year: "numeric",
                    })}{" "}
                    · {itemCount(order)}
                    {order.status === "CANCELLED" ? " · Cancelled" : ""}
                  </span>
                </span>

                <span className="tabular font-sans text-lg text-ink">
                  {formatPrice(order.total)}
                </span>
              </Link>
            </li>
          ))}
        </ul>
      )}

      {ordersQuery.isSuccess && ordersQuery.data.length > 0 && (
        <p className="font-sans text-xs text-muted">
          Totals are what you were charged at the time. They don&rsquo;t change
          if a book is repriced later.
        </p>
      )}
    </section>
  );
}
