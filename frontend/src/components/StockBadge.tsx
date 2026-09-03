interface StockBadgeProps {
  stock: number;
}

/** Honest stock state. Semantic colours, never the brand accent. */
export default function StockBadge({ stock }: StockBadgeProps) {
  if (stock === 0) {
    return <span className="font-sans text-xs font-medium text-bad">Out of stock</span>;
  }
  if (stock <= 5) {
    return (
      <span className="font-sans text-xs font-medium text-warn">Only {stock} left</span>
    );
  }
  return <span className="font-sans text-xs font-medium text-good">In stock</span>;
}
