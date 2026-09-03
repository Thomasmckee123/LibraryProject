import styles from "./QuantityStepper.module.css";

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

  return (
    <div className={styles.stepper}>
      <button
        type="button"
        className={styles.button}
        aria-label={`Decrease quantity of ${label}`}
        onClick={() => onChange(quantity - 1)}
        disabled={disabled || atMin}
      >
        −
      </button>
      <span className={styles.readout} aria-live="polite">
        {quantity}
      </span>
      <button
        type="button"
        className={styles.button}
        aria-label={`Increase quantity of ${label}`}
        onClick={() => onChange(quantity + 1)}
        disabled={disabled}
      >
        +
      </button>
    </div>
  );
}
