"use client";

import { useEffect, useRef, useState } from "react";
import type { MarketResponse } from "@/lib/types";

interface TickerItem {
  key: number;
  label: string;
  price: number;
  direction: -1 | 0 | 1;
}

export default function Ticker({ markets }: { markets: MarketResponse[] }) {
  const prev = useRef<Map<number, number>>(new Map());
  const [items, setItems] = useState<TickerItem[]>([]);

  useEffect(() => {
    const next = markets.map((market) => {
      const leading = [...market.outcomes].sort((a, b) => b.price - a.price)[0];
      const last = prev.current.get(leading.id);
      const direction = last === undefined || last === leading.price ? 0 : leading.price > last ? 1 : -1;
      prev.current.set(leading.id, leading.price);
      return { key: market.id, label: leading.label, price: leading.price, direction } as TickerItem;
    });
    setItems(next);
  }, [markets]);

  if (items.length === 0) return null;

  const row = (ariaHidden: boolean) => (
    <div aria-hidden={ariaHidden || undefined} className="flex shrink-0 items-center">
      {items.map((item) => (
        <span key={item.key} className="flex items-center gap-2 px-5 font-mono text-xs uppercase tracking-wider">
          <span className="text-paper/80">{item.label}</span>
          <span
            className={
              item.direction > 0 ? "text-emerald-300" : item.direction < 0 ? "text-rose-300" : "text-paper"
            }
          >
            {(item.price * 100).toFixed(1)}
            {item.direction > 0 ? " ▲" : item.direction < 0 ? " ▼" : ""}
          </span>
          <span className="pl-5 text-violet-300">◆</span>
        </span>
      ))}
    </div>
  );

  return (
    <div className="group relative left-1/2 mb-10 w-screen -translate-x-1/2 overflow-hidden border-y-2 border-ink bg-ink py-2">
      <div className="flex w-max animate-marquee group-hover:[animation-play-state:paused]">
        {row(false)}
        {row(true)}
      </div>
    </div>
  );
}
