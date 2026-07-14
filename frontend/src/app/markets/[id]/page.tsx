"use client";

import { useCallback, useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { apiFetch } from "@/lib/api";
import type { MarketResponse, PricePointResponse } from "@/lib/types";
import BetForm from "@/components/BetForm";
import PriceChart from "@/components/PriceChart";
import ProbabilityBar from "@/components/ProbabilityBar";

const POLL_INTERVAL_MS = 5000;

function MarketDetail() {
  const params = useParams<{ id: string }>();
  const [market, setMarket] = useState<MarketResponse | null>(null);
  const [history, setHistory] = useState<PricePointResponse[]>([]);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    try {
      const [marketData, historyData] = await Promise.all([
        apiFetch<MarketResponse>(`/api/markets/${params.id}`),
        apiFetch<PricePointResponse[]>(`/api/markets/${params.id}/history`),
      ]);
      setMarket(marketData);
      setHistory(historyData);
      setError(null);
    } catch {
      setError("could not load this market");
    }
  }, [params.id]);

  useEffect(() => {
    load();
    const timer = setInterval(load, POLL_INTERVAL_MS);
    return () => clearInterval(timer);
  }, [load]);

  if (error) return <p className="text-sm text-red-600">{error}</p>;
  if (!market) return <p className="text-sm text-gray-500">Loading market…</p>;

  return (
    <div>
      <div className="mb-1 text-xs font-medium uppercase tracking-wide text-violet-600">
        {market.status}
      </div>
      <h1 className="mb-4 text-2xl font-bold text-gray-900">{market.question}</h1>
      <div className="mb-6 rounded-lg border border-violet-100 bg-white p-4 shadow-sm">
        <ProbabilityBar outcomes={market.outcomes} />
      </div>
      <div className="grid gap-6 lg:grid-cols-[1fr_320px]">
        <div className="rounded-lg border border-violet-100 bg-white p-4 shadow-sm">
          <h3 className="mb-2 font-semibold text-gray-900">Price history</h3>
          <PriceChart history={history} outcomes={market.outcomes} />
        </div>
        <BetForm market={market} onPlaced={load} />
      </div>
    </div>
  );
}

export default function MarketDetailPage() {
  return <MarketDetail />;
}
