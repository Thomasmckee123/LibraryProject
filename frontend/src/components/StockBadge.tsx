/**
 * Renders stock honestly - never just "in stock" when it isn't.
 * Uses the shared .stock / .stock--in / --low / --out classes from index.css.
 */
export default function StockBadge({ stock }: { stock: number }) {
  if (stock === 0) {
    return <span className="stock stock--out">Out of stock</span>;
  }
  if (stock <= 5) {
    return <span className="stock stock--low">Only {stock} left</span>;
  }
  return <span className="stock stock--in">In stock</span>;
}
