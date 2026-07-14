const LINES = Array.from({ length: 36 }, (_, i) => {
  const angle = (i / 36) * Math.PI * 2;
  const jitter = ((i * 7919) % 23) / 23;
  const inner = 34 + jitter * 22;
  return {
    x1: 50 + Math.cos(angle) * inner,
    y1: 50 + Math.sin(angle) * inner,
    x2: 50 + Math.cos(angle) * 75,
    y2: 50 + Math.sin(angle) * 75,
    w: 0.35 + jitter * 0.75,
  };
});

export default function SpeedLines({ className = "" }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 100 100"
      aria-hidden="true"
      preserveAspectRatio="xMidYMid slice"
      className={`pointer-events-none absolute inset-0 h-full w-full ${className}`}
    >
      {LINES.map((l, i) => (
        <line
          key={i}
          x1={l.x1}
          y1={l.y1}
          x2={l.x2}
          y2={l.y2}
          stroke="currentColor"
          strokeWidth={l.w}
          strokeLinecap="round"
        />
      ))}
    </svg>
  );
}
