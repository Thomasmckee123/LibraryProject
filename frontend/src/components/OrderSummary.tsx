import type { ReactNode } from "react";
import { formatPrice } from "../api/money";

/**
 * A shared shape both a cart line and an order line can be mapped to, so
 * this one block can render "what's in the cart" on Checkout and "what was
 * bought" on OrderConfirmed. Money fields stay strings - see api/money.ts.
 */
export interface SummaryLine {
  isbn: string;
  title: string;
  author?: string;
  unitPrice: string;
  quantity: number;
  lineTotal: string;
}

interface OrderSummaryProps {
  lines: SummaryLine[];
  total: string;
  emptyMessage?: ReactNode;
}

/** Reusable line-items-and-total block. Never sums prices itself - the server total is rendered as-is. */
export default function OrderSummary({ lines, total, emptyMessage }: OrderSummaryProps) {
  if (lines.length === 0) {
    return <p className="font-sans text-sm text-muted">{emptyMessage ?? "No items."}</p>;
  }

  return (
    <div className="rounded border border-rule bg-surface">
      <ul className="divide-y divide-rule">
        {lines.map((line) => (
          <li key={line.isbn} className="flex items-start justify-between gap-4 p-4">
            <div className="flex flex-col gap-0.5">
              <span className="font-semibold text-ink">{line.title}</span>
              {line.author && (
                <span className="font-sans text-sm text-muted">{line.author}</span>
              )}
              <span className="font-sans text-xs text-muted">
                {line.quantity} × {formatPrice(line.unitPrice)}
              </span>
            </div>
            <span className="tabular font-semibold text-ink">
              {formatPrice(line.lineTotal)}
            </span>
          </li>
        ))}
      </ul>
      <div className="flex items-center justify-between border-t border-rule p-4">
        <span className="font-sans text-sm font-medium text-ink-soft">Total</span>
        <span className="tabular text-xl font-semibold text-ink">{formatPrice(total)}</span>
      </div>
    </div>
  );
}
