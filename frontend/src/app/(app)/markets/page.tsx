"use client";

import { useLiveMarkets } from "@/lib/live";
import Loading from "@/components/Loading";
import MarketCard from "@/components/market/MarketCard";
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
      <div className="grid gap-6 sm:grid-cols-2">
        {markets.map((market, index) => (
          <MarketCard key={market.id} market={market} index={index} />
        ))}
      </div>
    </>
  );
}

export default function MarketsPage() {
  return <MarketsBody />;
}
