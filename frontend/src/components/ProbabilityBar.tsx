"use client";

import { motion, useReducedMotion } from "motion/react";
import { outcomeColor } from "@/lib/palette";
import type { OutcomeResponse } from "@/lib/types";

export default function ProbabilityBar({ outcomes }: { outcomes: OutcomeResponse[] }) {
  const reduce = useReducedMotion();
  const leadingId = [...outcomes].sort((a, b) => b.price - a.price)[0]?.id;

  return (
    <div>
      <div className="flex h-2 w-full gap-[3px]">
        {outcomes.map((outcome) => (
          <motion.div
            key={outcome.id}
            className={`rounded-full ${outcome.id === leadingId ? "shadow-glow-sm" : ""}`}
            style={{ backgroundColor: outcomeColor(outcome.idx) }}
            initial={reduce ? false : { width: "2%" }}
            whileInView={{ width: `${Math.max(outcome.price * 100, 2)}%` }}
            viewport={{ once: true }}
            transition={{ type: "spring", stiffness: 60, damping: 18 }}
          />
        ))}
      </div>
      <div className="mt-2.5 flex flex-wrap gap-x-5 gap-y-1.5">
        {outcomes.map((outcome) => (
          <span key={outcome.id} className="flex items-baseline gap-1.5 text-xs text-ink/70">
            <span
              className="inline-block h-2 w-2 self-center rounded-full"
              style={{ backgroundColor: outcomeColor(outcome.idx) }}
            />
            {outcome.label}
            <span className="font-mono text-sm font-bold tabular-nums text-ink">
              {(outcome.price * 100).toFixed(1)}%
            </span>
          </span>
        ))}
      </div>
    </div>
  );
}
