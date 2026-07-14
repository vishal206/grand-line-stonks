export function Berry({ className = "h-[0.8em] w-auto" }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 13 15"
      aria-hidden="true"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.6"
      strokeLinecap="round"
      strokeLinejoin="round"
      className={`inline-block ${className}`}
    >
      <path d="M4.5 1.5v12" />
      <path d="M4.5 1.5h2.7a2.9 2.9 0 0 1 0 5.8H4.5" />
      <path d="M4.5 7.3h3.2a3.1 3.1 0 0 1 0 6.2H4.5" />
      <path d="M1 4.8h2.6" />
      <path d="M1 9.6h2.6" />
    </svg>
  );
}

export function BerryAmount({
  value,
  decimals = 2,
  signed = false,
  className = "",
}: {
  value: number;
  decimals?: number;
  signed?: boolean;
  className?: string;
}) {
  const formatted = Math.abs(value).toLocaleString(undefined, {
    minimumFractionDigits: 0,
    maximumFractionDigits: decimals,
  });
  const sign = value < 0 ? "-" : signed ? "+" : "";
  return (
    <span className={`inline-flex items-baseline gap-[0.15em] font-mono tabular-nums ${className}`}>
      {sign}
      <Berry />
      {formatted}
      <span className="sr-only">berries</span>
    </span>
  );
}
