"use client";

import { useParams } from "next/navigation";
import { useLiveMarket } from "@/lib/live";
import BetForm from "@/components/BetForm";
import PriceChart from "@/components/PriceChart";
import ProbabilityBar from "@/components/ProbabilityBar";

function MarketDetail() {
  const params = useParams<{ id: string }>();
  const { market, history, error, refresh } = useLiveMarket(params.id);

  if (error) return <p className="text-sm text-negative">{error}</p>;
  if (!market) return <p className="text-sm text-ink/60">Loading market…</p>;

  return (
    <div>
      <div className="mb-2 flex items-baseline gap-4">
        <span className="font-serif text-[11px] uppercase tracking-caps text-violet">
          Bounty Nº {market.id}
        </span>
        <span className="font-serif text-[11px] uppercase tracking-caps text-ink/50">
          {market.status}
        </span>
      </div>
      <h1 className="mb-6 max-w-3xl text-2xl font-semibold leading-snug text-ink sm:text-3xl">
        {market.question}
      </h1>
      <div className="mb-6 border border-ink/10 bg-white p-5">
        <ProbabilityBar outcomes={market.outcomes} />
      </div>
      <div className="grid items-start gap-6 lg:grid-cols-[1fr_340px]">
        <div className="border border-ink/10 bg-white p-5">
          <h3 className="mb-4 font-serif text-xs uppercase tracking-caps text-violet">
            Price history
          </h3>
          <PriceChart history={history} outcomes={market.outcomes} />
        </div>
        <BetForm market={market} onPlaced={refresh} />
      </div>
    </div>
  );
}

export default function MarketDetailPage() {
  return <MarketDetail />;
}
