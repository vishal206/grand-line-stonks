import Link from "next/link";
import Smoke from "@/components/Smoke";
import Reveal from "@/components/fx/Reveal";
import LandingWordmark from "@/components/LandingWordmark";

const capsLabel = "font-serif text-xs uppercase tracking-caps";

export default function Landing() {
  return (
    <div className="relative flex min-h-screen flex-col overflow-hidden bg-paper">
      <div aria-hidden="true" className="halftone absolute inset-x-0 top-[18%] h-[55%] opacity-60" />
      <Smoke />
      <header className="relative z-10 flex items-baseline justify-between px-6 py-5 sm:px-10">
        <Reveal y={-12}>
          <span className={`${capsLabel} text-ink`}>Grand Line Stonks</span>
        </Reveal>
        <Reveal y={-12} delay={0.08}>
          <nav className="flex gap-6 sm:gap-10">
            <Link href="/markets" className={`${capsLabel} text-ink transition-colors hover:text-violet`}>
              Markets
            </Link>
            <Link href="/login" className={`${capsLabel} text-ink transition-colors hover:text-violet`}>
              Log in
            </Link>
            <Link href="/signup" className={`${capsLabel} text-ink transition-colors hover:text-violet`}>
              Sign up
            </Link>
          </nav>
        </Reveal>
      </header>

      <div className="relative z-10 flex flex-1 flex-col justify-center py-12">
        <LandingWordmark />
      </div>

      <footer className="relative z-10 flex flex-col gap-8 px-6 pb-10 sm:flex-row sm:items-end sm:justify-between sm:px-10">
        <Reveal delay={0.45}>
          <p className="max-w-md text-base text-ink sm:text-lg">
            The play-money prediction market of the Grand Line. Stake berries on
            what happens next.
          </p>
        </Reveal>
        <Reveal delay={0.55}>
          <div className="flex flex-col items-start gap-4 sm:items-end">
            <Link
              href="/markets"
              className="bg-violet-vivid px-8 py-4 font-serif text-sm uppercase tracking-caps text-paper transition-all hover:bg-violet hover:shadow-glow"
            >
              Enter the market
            </Link>
            <span className={`${capsLabel} text-ink/60`}>Play money · No berries were harmed</span>
          </div>
        </Reveal>
      </footer>
    </div>
  );
}
