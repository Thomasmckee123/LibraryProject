import type { ReactNode } from "react";
import styles from "./EmptyState.module.css";

interface EmptyStateProps {
  /** What's missing, in plain language - e.g. "Your cart is empty." */
  message: string;
  /** An optional way out, usually a <Link> back to somewhere useful. */
  action?: ReactNode;
}

/**
 * Generic "there's nothing here" block. Used wherever a list can come back
 * empty - the cart today, other routes later - so it takes no domain-specific
 * props.
 */
export default function EmptyState({ message, action }: EmptyStateProps) {
  return (
    <div className={styles.empty}>
      <p className={styles.message}>{message}</p>
      {action ? <div className={styles.action}>{action}</div> : null}
    </div>
  );
}
