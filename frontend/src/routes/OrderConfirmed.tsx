import { useQuery } from "@tanstack/react-query";
import { Link, useParams } from "react-router-dom";
import type { OrderLine } from "../types";
import { getOrder } from "../api/orders";
import OrderSummary, { type SummaryLine } from "../components/OrderSummary";
import styles from "./OrderConfirmed.module.css";

function orderLineToSummaryLine(line: OrderLine): SummaryLine {
  return {
    isbn: line.isbn,
    title: line.titleAtPurchase,
    unitPrice: line.unitPrice,
    quantity: line.quantity,
    lineTotal: line.lineTotal,
  };
}

/**
 * Order confirmation screen at /orders/:reference.
 *
 * The route names its param "reference" for a readable URL, but
 * api/orders.ts fetches by the order's numeric id (GET /api/orders/{id}),
 * matching how Checkout builds the link on success (see Checkout.tsx). The
 * order's own `reference` field - the fake confirmation code - is still
 * what's shown to the customer below.
 */
export default function OrderConfirmed() {
  const { reference } = useParams<{ reference: string }>();
  const orderId = Number(reference);
  const hasValidId = reference !== undefined && reference !== "" && !Number.isNaN(orderId);

  const orderQuery = useQuery({
    queryKey: ["order", orderId],
    queryFn: () => getOrder(orderId),
    enabled: hasValidId,
  });

  if (!hasValidId) {
    return (
      <section className={styles.confirmed}>
        <h1>Order not found</h1>
        <p className="placeholder">
          &ldquo;{reference}&rdquo; isn&rsquo;t a valid order reference.{" "}
          <Link to="/">Back to the catalogue</Link>
        </p>
      </section>
    );
  }

  return (
    <section className={styles.confirmed}>
      <h1>Order confirmed</h1>

      {orderQuery.isPending && <p className="placeholder">Loading your order…</p>}

      {orderQuery.isError && (
        <p className={styles.error} role="alert">
          Could not load this order: {orderQuery.error.message}
        </p>
      )}

      {orderQuery.isSuccess && orderQuery.data.lines.length === 0 && (
        <p className="placeholder">This order has no lines.</p>
      )}

      {orderQuery.isSuccess && orderQuery.data.lines.length > 0 && (
        <>
          <p className={styles.reference}>
            Reference <strong>{orderQuery.data.reference}</strong>
          </p>
          <p className={styles.meta}>
            Placed {new Date(orderQuery.data.placedAt).toLocaleString("en-GB")} ·{" "}
            {orderQuery.data.status}
          </p>

          <OrderSummary
            lines={orderQuery.data.lines.map(orderLineToSummaryLine)}
            total={orderQuery.data.total}
          />

          <p className={styles.note}>
            The prices above are what you were actually charged - they stay
            fixed even if a book&rsquo;s price changes later.
          </p>

          <p>
            <Link to="/">Back to the catalogue</Link>
          </p>
        </>
      )}
    </section>
  );
}
