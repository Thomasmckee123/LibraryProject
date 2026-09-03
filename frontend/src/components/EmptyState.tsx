import type { ReactNode } from "react";

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
    <div className="flex flex-col items-center gap-3 rounded border border-dashed border-rule py-16 text-center">
      <p className="font-sans text-sm text-muted">{message}</p>
      {action ? <div className="font-sans text-sm text-accent">{action}</div> : null}
    </div>
  );
}
