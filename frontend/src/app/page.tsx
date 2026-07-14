import Link from "next/link";
import Smoke from "@/components/Smoke";

const capsLabel = "font-serif text-xs uppercase tracking-caps";

export default function Landing() {
  return (
    <div className="relative flex min-h-screen flex-col overflow-hidden bg-paper">
      <Smoke />
      <header className="relative z-10 flex items-baseline justify-between px-6 py-5 sm:px-10">
        <span className={`${capsLabel} text-ink`}>Grand Line Stonks</span>
        <nav className="flex gap-6 sm:gap-10">
          <Link href="/markets" className={`${capsLabel} text-ink hover:text-violet`}>
            Markets
          </Link>
          <Link href="/login" className={`${capsLabel} text-ink hover:text-violet`}>
            Log in
          </Link>
          <Link href="/signup" className={`${capsLabel} text-ink hover:text-violet`}>
            Sign up
          </Link>
        </nav>
      </header>

      <div className="relative z-10 flex flex-1 flex-col justify-center py-12">
        <h1 className="font-display text-wordmark uppercase text-violet">
          <span className="block whitespace-nowrap [margin-left:-0.05em]">Grand Line</span>
          <span className="block whitespace-nowrap [margin-left:12vw]">Stonks</span>
        </h1>
      </div>

      <footer className="relative z-10 flex flex-col gap-8 px-6 pb-10 sm:flex-row sm:items-end sm:justify-between sm:px-10">
        <p className="max-w-md text-base text-ink sm:text-lg">
          The play-money prediction market of the Grand Line. Stake berries on
          what happens next.
        </p>
        <div className="flex flex-col items-start gap-4 sm:items-end">
          <Link
            href="/markets"
            className="bg-violet-vivid px-8 py-4 font-serif text-sm uppercase tracking-caps text-paper hover:bg-violet"
          >
            Enter the market
          </Link>
          <span className={`${capsLabel} text-ink/60`}>Play money · No berries were harmed</span>
        </div>
      </footer>
    </div>
  );
}
