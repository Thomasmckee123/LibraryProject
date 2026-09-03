import { Link } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import type { Cart, CartLine } from "../types";
import { addItem, getCart, removeItem } from "../api/cart";
import { formatPrice } from "../api/money";
import EmptyState from "../components/EmptyState";
import QuantityStepper from "../components/QuantityStepper";
import styles from "./Cart.module.css";

// TODO: there is no login/session yet, so every visitor shares one cart.
// Replace with the signed-in customer's cart id once accounts exist.
const CART_ID = 1;

export default function CartPage() {
  const queryClient = useQueryClient();

  const cartQuery = useQuery({
    queryKey: ["cart", CART_ID],
    queryFn: () => getCart(CART_ID),
  });

  const invalidateCart = () =>
    queryClient.invalidateQueries({ queryKey: ["cart", CART_ID] });

  if (cartQuery.isPending) {
    return (
      <section>
        <h1>Cart</h1>
        <p className="placeholder">Loading your cart…</p>
      </section>
    );
  }

  if (cartQuery.isError) {
    return (
      <section>
        <h1>Cart</h1>
        <p className="placeholder" role="alert">
          Couldn't load your cart: {cartQuery.error.message}
        </p>
      </section>
    );
  }

  const cart: Cart = cartQuery.data;

  if (cart.lines.length === 0) {
    return (
      <section>
        <h1>Cart</h1>
        <EmptyState
          message="Your cart is empty."
          action={<Link to="/">Browse the catalogue</Link>}
        />
      </section>
    );
  }

  return (
    <section>
      <h1>Cart</h1>
      <table className={styles.table}>
        <thead>
          <tr>
            <th>Book</th>
            <th>Unit price</th>
            <th>Quantity</th>
            <th>Line total</th>
            <th aria-hidden="true" />
          </tr>
        </thead>
        <tbody>
          {cart.lines.map((line) => (
            <CartRow key={line.isbn} cartId={CART_ID} line={line} onMutated={invalidateCart} />
          ))}
        </tbody>
      </table>

      <div className={styles.summary}>
        <span className={styles.totalLabel}>Total</span>
        <span className={`${styles.totalValue} price`}>{formatPrice(cart.total)}</span>
      </div>

      <Link to="/checkout" className={styles.checkoutLink}>
        Proceed to checkout
      </Link>
    </section>
  );
}

interface CartRowProps {
  cartId: number;
  line: CartLine;
  onMutated: () => void;
}

/**
 * One cart line with its own mutation state, so adjusting one book's
 * quantity doesn't disable the controls on every other line.
 *
 * There is no "set quantity" endpoint on the API - only addItem (which
 * merges a quantity into the existing line) and removeItem (which drops
 * the line entirely). The stepper's target quantity is converted to a
 * delta and sent through addItem; going to zero is handled as an explicit
 * remove instead, never as a delta.
 */
function CartRow({ cartId, line, onMutated }: CartRowProps) {
  const quantityMutation = useMutation({
    mutationFn: (delta: number) => addItem(cartId, line.isbn, delta),
    onSuccess: onMutated,
  });

  const removeMutation = useMutation({
    mutationFn: () => removeItem(cartId, line.isbn),
    onSuccess: onMutated,
  });

  const pending = quantityMutation.isPending || removeMutation.isPending;
  const failed = quantityMutation.isError || removeMutation.isError;

  return (
    <>
      <tr>
        <td>
          <div className={styles.book}>
            <span className={styles.title}>{line.title}</span>
            <span className={styles.author}>{line.author}</span>
          </div>
        </td>
        <td className="price">{formatPrice(line.unitPrice)}</td>
        <td>
          <QuantityStepper
            quantity={line.quantity}
            label={line.title}
            disabled={pending}
            onChange={(next) => quantityMutation.mutate(next - line.quantity)}
          />
        </td>
        <td className="price">{formatPrice(line.lineTotal)}</td>
        <td>
          <button
            type="button"
            className="secondary"
            aria-label={`Remove ${line.title} from cart`}
            onClick={() => removeMutation.mutate()}
            disabled={pending}
          >
            Remove
          </button>
        </td>
      </tr>
      {failed && (
        <tr>
          <td colSpan={5} role="alert" className={styles.rowError}>
            Something went wrong updating "{line.title}". Please try again.
          </td>
        </tr>
      )}
    </>
  );
}
