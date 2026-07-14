const TONES = {
  violet: "border-violet-500 text-violet",
  ink: "border-ink/40 text-ink/70",
  pink: "border-pink text-pink",
  paper: "border-paper/70 text-paper",
} as const;

export default function Stamp({
  children,
  tone = "violet",
  className = "",
}: {
  children: React.ReactNode;
  tone?: keyof typeof TONES;
  className?: string;
}) {
  return (
    <span
      className={`inline-block -rotate-3 border-2 px-2 py-0.5 font-serif text-[10px] font-semibold uppercase tracking-caps ${TONES[tone]} ${className}`}
    >
      {children}
    </span>
  );
}
