"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import { BerryAmount } from "@/components/Berry";

function NavLink({ href, label }: { href: string; label: string }) {
  const pathname = usePathname();
  const active = pathname === href || pathname.startsWith(`${href}/`);
  return (
    <Link
      href={href}
      aria-current={active ? "page" : undefined}
      className={`text-sm ${active ? "font-medium text-violet" : "text-ink/70 hover:text-violet"}`}
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
            <BerryAmount value={me.balance} className="text-sm font-bold text-violet" />
            <button
              onClick={() => {
                logout();
                router.push("/login");
              }}
              className="border border-ink/20 px-3 py-1.5 text-sm text-ink/70 hover:border-violet hover:text-violet"
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
              className="bg-violet-vivid px-4 py-1.5 text-sm text-paper hover:bg-violet"
            >
              Sign up
            </Link>
          </div>
        )}
      </div>
    </nav>
  );
}
