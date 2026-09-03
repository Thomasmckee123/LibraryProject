interface QuantityStepperProps {
  quantity: number;
  /** Label used to build per-control aria-labels, e.g. a book title. */
  label: string;
  /** Called with the next quantity when + or - is pressed. */
  onChange: (quantity: number) => void;
  /** Floor for the stepper. Removal is a separate action, not zero. */
  min?: number;
  disabled?: boolean;
}

/**
 * +/- quantity control. Never steps below `min` (default 1) - taking a line
 * to zero is a removal, which is a distinct, explicit action elsewhere.
 */
export default function QuantityStepper({
  quantity,
  label,
  onChange,
  min = 1,
  disabled = false,
}: QuantityStepperProps) {
  const atMin = quantity <= min;

  const button =
    "flex h-7 w-7 items-center justify-center rounded border border-rule " +
    "font-sans text-ink transition-colors hover:border-accent hover:text-accent " +
    "disabled:cursor-not-allowed disabled:opacity-40 disabled:hover:border-rule " +
    "disabled:hover:text-ink";

  return (
    <div className="inline-flex items-center gap-2">
      <button
        type="button"
        className={button}
        aria-label={`Decrease quantity of ${label}`}
        onClick={() => onChange(quantity - 1)}
        disabled={disabled || atMin}
      >
        −
      </button>
      <span className="tabular w-6 text-center font-sans text-sm" aria-live="polite">
        {quantity}
      </span>
      <button
        type="button"
        className={button}
        aria-label={`Increase quantity of ${label}`}
        onClick={() => onChange(quantity + 1)}
        disabled={disabled}
      >
        +
      </button>
    </div>
  );
}
