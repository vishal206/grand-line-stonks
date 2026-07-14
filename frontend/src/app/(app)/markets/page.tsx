"use client";

import { useState } from "react";
import { useLiveMarkets } from "@/lib/live";
import type { MarketStatus } from "@/lib/types";
import Loading from "@/components/Loading";
import Controls from "@/components/market/Controls";
import MarketCard from "@/components/market/MarketCard";
import Ticker from "@/components/market/Ticker";
import TrendingHero from "@/components/market/TrendingHero";

const EMPTY_COPY: Partial<Record<MarketStatus, string>> = {
  OPEN: "No open bounties yet — the board is bare",
  CLOSED: "Nothing awaiting judgement",
  SETTLED: "No bounties settled yet",
};

function MarketsBody() {
  const [status, setStatus] = useState<MarketStatus>("OPEN");
  const [sort, setSort] = useState<"asc" | "desc">("desc");
  const { markets, error } = useLiveMarkets(status, sort);

  if (error) return <p className="text-sm text-negative">{error}</p>;
  if (!markets) return <Loading label="Scanning the horizon" />;

  // trending is meant to come from a backend endpoint (not built yet); newest open market stands in
  const featured = status === "OPEN" && sort === "desc" ? markets[0] : null;

  return (
    <>
      {status === "OPEN" && markets.length > 0 && <Ticker markets={markets} />}
      <div className="mb-8">
        <p className="font-serif text-xs uppercase tracking-caps text-ink/50">The bounty board</p>
        <h1 className="mt-1 text-2xl font-semibold text-ink">Markets</h1>
      </div>
      {featured && <TrendingHero market={featured} />}
      <Controls status={status} onStatus={setStatus} sort={sort} onSort={setSort} />
      {markets.length === 0 ? (
        <p className="py-10 text-center font-serif text-xs uppercase tracking-caps text-ink/50">
          {EMPTY_COPY[status] ?? "Nothing here yet"}
        </p>
      ) : (
        <div className="grid gap-6 sm:grid-cols-2">
          {markets.map((market, index) => (
            <MarketCard key={market.id} market={market} index={index} />
          ))}
        </div>
      )}
    </>
  );
}

export default function MarketsPage() {
  return <MarketsBody />;
}
