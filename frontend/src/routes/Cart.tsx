import { Link } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import type { Cart, CartLine } from "../types";
import { getCart, removeItem, setQuantity } from "../api/cart";
import { formatPrice } from "../api/money";
import EmptyState from "../components/EmptyState";
import QuantityStepper from "../components/QuantityStepper";

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
        <h1 className="mb-6 text-4xl font-semibold">Cart</h1>
        <p className="placeholder">Loading your cart…</p>
      </section>
    );
  }

  if (cartQuery.isError) {
    return (
      <section>
        <h1 className="mb-6 text-4xl font-semibold">Cart</h1>
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
        <h1 className="mb-6 text-4xl font-semibold">Cart</h1>
        <EmptyState
          message="Your cart is empty."
          action={<Link to="/">Browse the catalogue</Link>}
        />
      </section>
    );
  }

  return (
    <section className="flex flex-col">
      <h1 className="mb-6 text-4xl font-semibold">Cart</h1>
      <table className="w-full border-collapse text-left">
        <thead>
          <tr>
            <th className="border-b border-rule pb-2 font-sans text-xs font-medium tracking-wide text-muted uppercase">Book</th>
            <th className="border-b border-rule pb-2 font-sans text-xs font-medium tracking-wide text-muted uppercase">Unit price</th>
            <th className="border-b border-rule pb-2 font-sans text-xs font-medium tracking-wide text-muted uppercase">Quantity</th>
            <th className="border-b border-rule pb-2 font-sans text-xs font-medium tracking-wide text-muted uppercase">Line total</th>
            <th className="border-b border-rule pb-2 font-sans text-xs font-medium tracking-wide text-muted uppercase" aria-hidden="true" />
          </tr>
        </thead>
        <tbody>
          {cart.lines.map((line) => (
            <CartRow key={line.isbn} cartId={CART_ID} line={line} onMutated={invalidateCart} />
          ))}
        </tbody>
      </table>

      <div className="mt-6 flex items-center justify-between border-t border-rule pt-4">
        <span className="font-sans text-sm font-medium text-ink-soft">Total</span>
        <span className="tabular text-2xl font-semibold text-ink">{formatPrice(cart.total)}</span>
      </div>

      <Link to="/checkout" className="mt-6 inline-block w-fit self-start rounded bg-accent px-5 py-2.5 font-sans text-sm text-surface transition-opacity hover:opacity-90">
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
 * The stepper sends an absolute quantity via setQuantity. It must not send
 * a delta through addItem: a decrease has no positive delta, and the
 * backend rejects a negative quantity with 400.
 */
function CartRow({ cartId, line, onMutated }: CartRowProps) {
  const quantityMutation = useMutation({
    mutationFn: (quantity: number) => setQuantity(cartId, line.isbn, quantity),
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
        <td className="py-4 align-top">
          <div className="flex flex-col gap-0.5">
            <span className="font-semibold text-ink">{line.title}</span>
            <span className="font-sans text-sm text-muted">{line.author}</span>
          </div>
        </td>
        <td className="tabular py-4 align-top">{formatPrice(line.unitPrice)}</td>
        <td className="py-4 align-top">
          <QuantityStepper
            quantity={line.quantity}
            label={line.title}
            disabled={pending}
            onChange={(next) => quantityMutation.mutate(next)}
          />
        </td>
        <td className="tabular py-4 align-top font-semibold">{formatPrice(line.lineTotal)}</td>
        <td className="py-4 align-top">
          <button
            type="button"
            className="rounded border border-rule px-3 py-1.5 font-sans text-sm text-accent transition-colors hover:border-accent disabled:cursor-not-allowed disabled:opacity-40"
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
          <td colSpan={5} role="alert" className="pb-3 font-sans text-sm text-bad">
            Something went wrong updating "{line.title}". Please try again.
          </td>
        </tr>
      )}
    </>
  );
}
