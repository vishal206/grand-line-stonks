"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";

export default function Nav() {
  const { me, logout } = useAuth();
  const router = useRouter();

  return (
    <nav className="border-b border-violet-100 bg-white">
      <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-3">
        <div className="flex items-center gap-6">
          <Link href="/" className="text-lg font-bold text-violet-700">
            Grand Line Stonks
          </Link>
          <Link href="/" className="text-sm text-gray-600 hover:text-violet-700">
            Markets
          </Link>
          {me && (
            <Link href="/portfolio" className="text-sm text-gray-600 hover:text-violet-700">
              Portfolio
            </Link>
          )}
        </div>
        {me ? (
          <div className="flex items-center gap-4">
            <span className="text-sm text-gray-600">
              {me.username} ·{" "}
              <span className="font-semibold text-violet-700">
                {Number(me.balance).toLocaleString()} ฿
              </span>
            </span>
            <button
              onClick={() => {
                logout();
                router.push("/login");
              }}
              className="rounded border border-gray-300 px-3 py-1 text-sm text-gray-600 hover:bg-gray-50"
            >
              Log out
            </button>
          </div>
        ) : (
          <div className="flex items-center gap-3">
            <Link href="/login" className="text-sm text-gray-600 hover:text-violet-700">
              Log in
            </Link>
            <Link
              href="/signup"
              className="rounded bg-violet-600 px-3 py-1 text-sm text-white hover:bg-violet-700"
            >
              Sign up
            </Link>
          </div>
        )}
      </div>
    </nav>
  );
}
