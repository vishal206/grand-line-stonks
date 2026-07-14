"use client";

import { motion } from "motion/react";
import type { MarketStatus } from "@/lib/types";

const TABS: MarketStatus[] = ["OPEN", "CLOSED", "SETTLED"];

export default function Controls({
  status,
  onStatus,
  sort,
  onSort,
}: {
  status: MarketStatus;
  onStatus: (s: MarketStatus) => void;
  sort: "asc" | "desc";
  onSort: (s: "asc" | "desc") => void;
}) {
  return (
    <div className="mb-8 flex flex-wrap items-end justify-between gap-4">
      <div role="tablist" aria-label="Market status" className="flex gap-7">
        {TABS.map((tab) => (
          <button
            key={tab}
            role="tab"
            aria-selected={status === tab}
            onClick={() => onStatus(tab)}
            className={`relative pb-2 font-serif text-xs uppercase tracking-caps transition-colors ${
              status === tab ? "text-violet" : "text-ink/50 hover:text-ink"
            }`}
          >
            {tab}
            {status === tab && (
              <motion.span
                layoutId="tab-ink"
                className="absolute inset-x-0 bottom-0 h-[5px] -rotate-1 rounded-[40%_60%_55%_45%/60%_40%_60%_40%] bg-violet"
                transition={{ type: "spring", stiffness: 400, damping: 32 }}
              />
            )}
          </button>
        ))}
      </div>
      <div className="flex gap-2">
        {(["desc", "asc"] as const).map((dir) => (
          <button
            key={dir}
            aria-pressed={sort === dir}
            onClick={() => onSort(dir)}
            className={`border-2 px-3 py-1 text-xs transition-colors ${
              sort === dir
                ? "border-violet bg-lilac text-violet"
                : "border-ink/15 text-ink/60 hover:border-ink/40 hover:text-ink"
            }`}
          >
            {dir === "desc" ? "Newest" : "Oldest"}
          </button>
        ))}
      </div>
    </div>
  );
}
