export default function Loading({
  label = "Reading the sea",
  className = "h-40",
}: {
  label?: string;
  className?: string;
}) {
  return (
    <div role="status" className={`relative flex items-center justify-center overflow-hidden ${className}`}>
      <div aria-hidden="true" className="pointer-events-none absolute inset-0">
        <div className="absolute left-[20%] top-[25%] h-1/2 w-2/5 rounded-full bg-lilac opacity-80 blur-2xl animate-drift motion-reduce:animate-none" />
        <div className="absolute right-[20%] top-[35%] h-1/2 w-2/5 rounded-full bg-cloud blur-2xl animate-drift-slow motion-reduce:animate-none" />
      </div>
      <p className="relative font-serif text-xs uppercase tracking-caps text-ink/50">{label}…</p>
    </div>
  );
}
