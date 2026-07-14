"use client";

import { useEffect, useState } from "react";
import { apiFetch } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { BerryAmount } from "@/components/Berry";
import Smoke from "@/components/Smoke";
import type { MarketResponse, Page, PositionResponse } from "@/lib/types";

const fullBleed = "relative left-1/2 w-screen -translate-x-1/2 overflow-hidden";

export default function ResultHero({ market }: { market: MarketResponse }) {
  const { me } = useAuth();
  const [positions, setPositions] = useState<PositionResponse[] | null>(null);

  const settled = market.status === "SETTLED" || market.status === "CANCELLED";

  useEffect(() => {
    if (!me || !settled) return;
    let cancelled = false;
    apiFetch<Page<PositionResponse>>("/api/me/positions?size=200")
      .then((page) => {
        if (!cancelled) setPositions(page.content);
      })
      .catch(() => {});
    return () => {
      cancelled = true;
    };
  }, [me?.userId, market.id, settled]); // eslint-disable-line react-hooks/exhaustive-deps

  if (!me || !settled || !positions) return null;
  const mine = positions.filter((p) => p.marketId === market.id);
  if (mine.length === 0) return null;

  if (market.status === "CANCELLED") {
    return (
      <div className={`${fullBleed} mb-10 bg-cloud px-6 py-16 text-center`}>
        <p className="font-serif text-xs uppercase tracking-caps text-ink/60">Bounty withdrawn</p>
        <p className="mt-4 font-display text-display-md uppercase text-ink">Market cancelled</p>
        <p className="mx-auto mt-4 max-w-md text-sm text-ink/70">
          This market was cancelled. Every berry you staked has been refunded.
        </p>
      </div>
    );
  }

  const winners = mine.filter((p) => p.won);
  const payout = winners.reduce((sum, p) => sum + p.shares, 0);
  const winningOutcome = market.outcomes.find((o) => o.id === market.winningOutcomeId);

  if (winners.length === 0) {
    return (
      <div className={`${fullBleed} mb-10 bg-cloud px-6 py-16 text-center`}>
        <p className="font-serif text-xs uppercase tracking-caps text-ink/60">Bounty settled</p>
        <p className="mt-4 font-display text-display-md uppercase text-ink">
          The sea decided otherwise
        </p>
        {winningOutcome && (
          <p className="mt-4 text-sm text-ink/70">
            This one resolved to <span className="font-medium text-ink">{winningOutcome.label}</span>.
            Better winds next voyage.
          </p>
        )}
      </div>
    );
  }

  return (
    <div className={`${fullBleed} mb-10 bg-violet px-6 py-20 text-center text-paper`}>
      <Smoke className="opacity-20" />
      <div className="relative">
        <p className="font-serif text-xs uppercase tracking-caps text-paper/70">
          Bounty collected{winningOutcome ? ` · ${winningOutcome.label}` : ""}
        </p>
        <p className="mt-6 font-display text-display-lg uppercase">You called it</p>
        <BerryAmount
          value={payout}
          className="mt-8 font-display text-display-md text-paper"
        />
        <p className="mt-6 text-sm text-paper/80">paid out to your balance</p>
      </div>
    </div>
  );
}
