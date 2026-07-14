"use client";

import Link from "next/link";
import { useLiveMarkets } from "@/lib/live";
import ProbabilityBar from "@/components/ProbabilityBar";
import Loading from "@/components/Loading";
import Ticker from "@/components/market/Ticker";
import TrendingHero from "@/components/market/TrendingHero";

function MarketsBody() {
  const { markets, error } = useLiveMarkets("OPEN");

  if (error) return <p className="text-sm text-negative">{error}</p>;
  if (!markets) return <Loading label="Scanning the horizon" />;
  if (markets.length === 0)
    return (
      <p className="py-10 text-center font-serif text-xs uppercase tracking-caps text-ink/50">
        No open bounties yet — the board is bare
      </p>
    );

  // trending is meant to come from a backend endpoint (not built yet); newest open market stands in
  const featured = markets[0];

  return (
    <>
      <Ticker markets={markets} />
      <div className="mb-8">
        <p className="font-serif text-xs uppercase tracking-caps text-ink/50">The bounty board</p>
        <h1 className="mt-1 text-2xl font-semibold text-ink">Open markets</h1>
      </div>
      <TrendingHero market={featured} />
      <div className="grid gap-5 sm:grid-cols-2">
        {markets.map((market) => (
          <Link
            key={market.id}
            href={`/markets/${market.id}`}
            className="border border-ink/10 bg-white p-5 transition-colors hover:border-violet-vivid"
          >
            <div className="mb-2 flex items-baseline justify-between">
              <span className="font-serif text-[11px] uppercase tracking-caps text-violet">
                Bounty Nº {market.id}
              </span>
              <span className="font-serif text-[11px] uppercase tracking-caps text-ink/50">
                {market.status}
              </span>
            </div>
            <h2 className="mb-4 font-medium leading-snug text-ink">{market.question}</h2>
            <ProbabilityBar outcomes={market.outcomes} />
          </Link>
        ))}
      </div>
    </>
  );
}

export default function MarketsPage() {
  return <MarketsBody />;
}
