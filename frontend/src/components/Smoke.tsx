export default function Smoke({ className = "" }: { className?: string }) {
  return (
    <div aria-hidden="true" className={`pointer-events-none absolute inset-0 overflow-hidden ${className}`}>
      <div className="absolute left-[5%] top-[20%] h-[45%] w-[55%] rounded-full bg-white opacity-80 blur-3xl animate-drift motion-reduce:animate-none" />
      <div className="absolute right-[0%] top-[45%] h-[50%] w-[45%] rounded-full bg-white opacity-60 blur-3xl animate-drift-slow motion-reduce:animate-none" />
      <div className="absolute bottom-[5%] left-[30%] h-[35%] w-[40%] rounded-full bg-cloud opacity-70 blur-3xl animate-drift-slow motion-reduce:animate-none [animation-delay:-8s]" />
    </div>
  );
}
