import type { ReactNode } from "react";
import { formatPrice } from "../api/money";
import styles from "./OrderSummary.module.css";

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
    return <p className={styles.empty}>{emptyMessage ?? "No items."}</p>;
  }

  return (
    <div className={styles.summary}>
      <ul className={styles.lines}>
        {lines.map((line) => (
          <li key={line.isbn} className={styles.line}>
            <div className={styles.lineInfo}>
              <span className={styles.lineTitle}>{line.title}</span>
              {line.author && <span className={styles.lineAuthor}>{line.author}</span>}
              <span className={styles.lineQty}>
                {line.quantity} × {formatPrice(line.unitPrice)}
              </span>
            </div>
            <span className={`price ${styles.lineTotal}`}>{formatPrice(line.lineTotal)}</span>
          </li>
        ))}
      </ul>
      <div className={styles.totalRow}>
        <span>Total</span>
        <span className={`price ${styles.total}`}>{formatPrice(total)}</span>
      </div>
    </div>
  );
}
