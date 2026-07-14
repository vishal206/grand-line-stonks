"use client";

import Link from "next/link";
import { useLiveMarkets } from "@/lib/live";
import ProbabilityBar from "@/components/ProbabilityBar";

function MarketList() {
  const { markets, error } = useLiveMarkets("OPEN");

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

export default function Home() {
  return (
    <>
      <h1 className="mb-6 text-2xl font-bold text-gray-900">Stock market</h1>
      <MarketList />
    </>
  );
}
