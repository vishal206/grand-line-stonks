"use client";

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import { apiFetch } from "@/lib/api";
import { RequireAuth } from "@/lib/auth";
import type { MarketResponse, Page } from "@/lib/types";
import ProbabilityBar from "@/components/ProbabilityBar";

const POLL_INTERVAL_MS = 5000;

function MarketList() {
  const [markets, setMarkets] = useState<MarketResponse[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    try {
      const page = await apiFetch<Page<MarketResponse>>("/api/markets?status=OPEN&size=50");
      setMarkets(page.content);
      setError(null);
    } catch {
      setError("could not load markets");
    }
  }, []);

  useEffect(() => {
    load();
    const timer = setInterval(load, POLL_INTERVAL_MS);
    return () => clearInterval(timer);
  }, [load]);

  if (error) return <p className="text-sm text-red-600">{error}</p>;
  if (!markets) return <p className="text-sm text-gray-500">Loading markets…</p>;
  if (markets.length === 0)
    return <p className="text-sm text-gray-500">No open markets right now.</p>;

  return (
    <div className="grid gap-4 sm:grid-cols-2">
      {markets.map((market) => (
        <Link
          key={market.id}
          href={`/markets/${market.id}`}
          className="rounded-lg border border-violet-100 bg-white p-4 shadow-sm transition hover:border-violet-300"
        >
          <h2 className="mb-3 font-medium text-gray-900">{market.question}</h2>
          <ProbabilityBar outcomes={market.outcomes} />
        </Link>
      ))}
    </div>
  );
}

export default function MarketsPage() {
  return (
    <RequireAuth>
      <h1 className="mb-6 text-2xl font-bold text-gray-900">Open markets</h1>
      <MarketList />
    </RequireAuth>
  );
}
