"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import { Berry } from "@/components/Berry";
import CountUp from "@/components/fx/CountUp";

function NavLink({ href, label }: { href: string; label: string }) {
  const pathname = usePathname();
  const active = pathname === href || pathname.startsWith(`${href}/`);
  return (
    <Link
      href={href}
      aria-current={active ? "page" : undefined}
      className={`text-sm transition-colors ${active ? "font-medium text-violet" : "text-ink/70 hover:text-violet"}`}
    >
      {label}
    </Link>
  );
}

export default function Nav() {
  const { me, logout } = useAuth();
  const router = useRouter();

  return (
    <nav className="border-b border-ink/10 bg-paper">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
        <div className="flex items-baseline gap-8">
          <Link href="/" className="font-display text-sm uppercase tracking-tight text-violet">
            Grand Line Stonks
          </Link>
          <NavLink href="/markets" label="Markets" />
          {me && <NavLink href="/portfolio" label="Portfolio" />}
        </div>
        {me ? (
          <div className="flex items-center gap-5">
            <span className="text-sm text-ink/70">{me.username}</span>
            <span className="inline-flex items-center gap-2">
              <span
                aria-hidden="true"
                className="inline-block h-1.5 w-1.5 animate-pulse-dot rounded-full bg-pink"
              />
              <span className="inline-flex items-baseline gap-[0.15em] font-mono text-sm font-bold text-violet">
                <Berry />
                <CountUp value={me.balance} decimals={2} />
                <span className="sr-only">berries</span>
              </span>
            </span>
            <button
              onClick={() => {
                logout();
                router.push("/login");
              }}
              className="border border-ink/20 px-3 py-1.5 text-sm text-ink/70 transition-colors hover:border-violet hover:text-violet"
            >
              Log out
            </button>
          </div>
        ) : (
          <div className="flex items-center gap-5">
            <Link href="/login" className="text-sm text-ink/70 hover:text-violet">
              Log in
            </Link>
            <Link
              href="/signup"
              className="bg-violet-vivid px-4 py-1.5 text-sm text-paper transition-all hover:bg-violet hover:shadow-glow-sm"
            >
              Sign up
            </Link>
          </div>
        )}
      </div>
    </nav>
  );
}
