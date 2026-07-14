"use client";

import { useCallback, useEffect, useState } from "react";
import { apiFetch } from "./api";
import type { MarketResponse, Page, PricePointResponse } from "./types";

// Transport is short-polling for now; screens only see the returned state.
const POLL_INTERVAL_MS = 5000;

function usePoll(load: () => Promise<void>) {
  useEffect(() => {
    let cancelled = false;
    const run = () => {
      if (!cancelled) load();
    };
    run();
    const timer = setInterval(run, POLL_INTERVAL_MS);
    return () => {
      cancelled = true;
      clearInterval(timer);
    };
  }, [load]);
}

export function useLiveMarkets(status: string, sort: "asc" | "desc" = "desc") {
  const [markets, setMarkets] = useState<MarketResponse[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    try {
      const page = await apiFetch<Page<MarketResponse>>(
        `/api/markets?status=${status}&size=50&sort=createdAt,${sort}`,
      );
      setMarkets(page.content);
      setError(null);
    } catch {
      setError("could not load markets");
    }
  }, [status, sort]);

  usePoll(load);
  return { markets, error };
}

export function useLiveMarket(marketId: string) {
  const [market, setMarket] = useState<MarketResponse | null>(null);
  const [history, setHistory] = useState<PricePointResponse[]>([]);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    try {
      const [marketData, historyData] = await Promise.all([
        apiFetch<MarketResponse>(`/api/markets/${marketId}`),
        apiFetch<PricePointResponse[]>(`/api/markets/${marketId}/history`),
      ]);
      setMarket(marketData);
      setHistory(historyData);
      setError(null);
    } catch {
      setError("could not load this market");
    }
  }, [marketId]);

  usePoll(load);
  return { market, history, error, refresh: load };
}
