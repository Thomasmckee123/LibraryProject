import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link, useNavigate } from "react-router-dom";
import type { Cart } from "../types";
import { api, ApiClientError } from "../api/client";
import { checkout } from "../api/orders";
import OrderSummary, { type SummaryLine } from "../components/OrderSummary";
import styles from "./Checkout.module.css";

// TODO: there is no auth yet, so checkout always acts on this one demo
// cart. Once customers can log in, this should come from the session
// instead of being hardcoded.
const CART_ID = 1;

function cartToSummaryLines(cart: Cart): SummaryLine[] {
  return cart.lines.map((line) => ({
    isbn: line.isbn,
    title: line.title,
    author: line.author,
    unitPrice: line.unitPrice,
    quantity: line.quantity,
    lineTotal: line.lineTotal,
  }));
}

/**
 * Confirm-and-place-order screen. Reads the cart directly through the
 * shared `api` client rather than a dedicated api/cart.ts - that file
 * belongs to another agent working on Cart.tsx concurrently, and this is
 * the one read Checkout needs from it.
 */
export default function Checkout() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const cartQuery = useQuery({
    queryKey: ["cart", CART_ID],
    queryFn: () => api.get<Cart>(`/carts/${CART_ID}`),
  });

  const placeOrder = useMutation({
    mutationFn: () => checkout(CART_ID),
    onSuccess: (order) => {
      // Stock just changed, so the cart the customer is looking at is stale.
      queryClient.invalidateQueries({ queryKey: ["cart", CART_ID] });
      // The order route names its param "reference" for a readable URL,
      // but the order API is keyed by numeric id - see api/orders.ts and
      // the matching comment in OrderConfirmed.tsx.
      navigate(`/orders/${order.id}`);
    },
  });

  return (
    <section className={styles.checkout}>
      <h1>Checkout</h1>

      {cartQuery.isPending && <p className="placeholder">Loading your cart…</p>}

      {cartQuery.isError && (
        <p className={styles.error} role="alert">
          Could not load your cart: {cartQuery.error.message}
        </p>
      )}

      {cartQuery.isSuccess && cartQuery.data.lines.length === 0 && (
        <p className="placeholder">
          Your cart is empty. <Link to="/">Browse the catalogue</Link> to add
          something first.
        </p>
      )}

      {cartQuery.isSuccess && cartQuery.data.lines.length > 0 && (
        <>
          <OrderSummary
            lines={cartToSummaryLines(cartQuery.data)}
            total={cartQuery.data.total}
          />

          <p className={styles.simulatedNote}>
            This is a learning project: checkout is simulated. No card
            details are collected and no real money moves. Placing the order
            reserves the stock and records it as paid.
          </p>

          {placeOrder.isError &&
            placeOrder.error instanceof ApiClientError &&
            placeOrder.error.isOutOfStock && (
              <div className={styles.outOfStock} role="alert">
                <p>
                  Sorry — <strong>{placeOrder.error.body.isbn ?? "one of these books"}</strong>{" "}
                  only has {placeOrder.error.body.available ?? 0}{" "}
                  {placeOrder.error.body.available === 1 ? "copy" : "copies"} left, but this
                  order needs {placeOrder.error.body.requested ?? "more than that"}. Someone
                  else must have bought it first.
                </p>
                <p>
                  <Link to="/cart">Go back to your cart</Link> to adjust the quantity.
                </p>
              </div>
            )}

          {placeOrder.isError &&
            !(placeOrder.error instanceof ApiClientError && placeOrder.error.isOutOfStock) && (
              <p className={styles.error} role="alert">
                Placing the order failed: {placeOrder.error.message}
              </p>
            )}

          <button type="button" onClick={() => placeOrder.mutate()} disabled={placeOrder.isPending}>
            {placeOrder.isPending ? "Placing order…" : "Place order"}
          </button>
        </>
      )}
    </section>
  );
}
