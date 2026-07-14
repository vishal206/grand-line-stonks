"use client";

import Link from "next/link";
import { useLiveMarket } from "@/lib/live";
import { outcomeColor } from "@/lib/palette";
import type { MarketResponse } from "@/lib/types";
import CountUp from "@/components/fx/CountUp";
import Reveal from "@/components/fx/Reveal";
import Stamp from "@/components/fx/Stamp";
import HeroChart from "@/components/market/HeroChart";

export default function TrendingHero({ market: initial }: { market: MarketResponse }) {
  const { market: live, history } = useLiveMarket(String(initial.id));
  const market = live ?? initial;
  const leading = [...market.outcomes].sort((a, b) => b.price - a.price)[0];

  return (
    <Reveal className="mb-14">
      <section className="relative border-[3px] border-ink bg-white shadow-[10px_10px_0_#3B1477]">
        <div aria-hidden="true" className="halftone-dense absolute inset-x-0 top-0 h-28" />
        <div className="relative grid gap-8 p-6 sm:p-8 lg:grid-cols-[minmax(0,5fr)_minmax(0,7fr)]">
          <div className="flex flex-col">
            <div className="mb-4 flex items-center gap-3">
              <Stamp tone="pink">Trending</Stamp>
              <span className="font-serif text-[11px] uppercase tracking-caps text-violet">
                Bounty Nº {market.id}
              </span>
            </div>
            <h2 className="mb-6 font-display text-2xl uppercase leading-tight text-ink sm:text-3xl">
              {market.question}
            </h2>
            <div className="mb-8 flex flex-wrap gap-3">
              {market.outcomes.map((outcome) => (
                <div
                  key={outcome.id}
                  className={`flex items-baseline gap-2 border-2 bg-white px-3 py-2 ${
                    outcome.id === leading.id ? "border-violet-500 shadow-glow-sm" : "border-ink/10"
                  }`}
                >
                  <span
                    className="inline-block h-2 w-2 self-center"
                    style={{ backgroundColor: outcomeColor(outcome.idx) }}
                  />
                  <span className="text-sm text-ink/80">{outcome.label}</span>
                  <span className="font-mono text-lg font-bold text-ink">
                    <CountUp value={outcome.price * 100} decimals={1} />%
                  </span>
                </div>
              ))}
            </div>
            <Link
              href={`/markets/${market.id}`}
              className="mt-auto inline-block w-fit bg-pink px-6 py-3 font-serif text-sm uppercase tracking-caps text-white transition-all hover:bg-violet hover:shadow-glow"
            >
              Place your bet →
            </Link>
          </div>
          <div className="relative border-2 border-ink/10 bg-white p-3">
            <HeroChart
              history={history}
              outcomeIdx={leading.idx}
              outcomeLabel={leading.label}
            />
          </div>
        </div>
      </section>
    </Reveal>
  );
}
