/**
 * The backend computes every total. This only renders them.
 *
 * Two implementations of the same arithmetic eventually disagree, and the one
 * the customer sees is the one that matters - so there is deliberately no
 * add, multiply, or sum here.
 */
const formatter = new Intl.NumberFormat("en-GB", {
  style: "currency",
  currency: "GBP",
});

export function formatPrice(amount: string): string {
  return formatter.format(Number(amount));
}
