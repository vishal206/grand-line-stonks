"use client";

import Link from "next/link";
import { motion, useReducedMotion } from "motion/react";
import type { MarketResponse } from "@/lib/types";
import ProbabilityBar from "@/components/ProbabilityBar";
import Stamp from "@/components/fx/Stamp";

export default function MarketCard({ market, index }: { market: MarketResponse; index: number }) {
  const reduce = useReducedMotion();
  return (
    <motion.div
      className="relative"
      initial={{ opacity: 0, y: 24 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, margin: "-30px" }}
      transition={
        reduce
          ? { duration: 0 }
          : { duration: 0.5, delay: (index % 6) * 0.07, ease: [0.22, 1, 0.36, 1] }
      }
      whileHover={reduce ? undefined : { y: -5, scale: 1.01 }}
    >
      <Link
        href={`/markets/${market.id}`}
        className="block border-2 border-ink/15 bg-white p-5 pt-7 shadow-[5px_5px_0_rgba(59,20,119,0.12)] transition-[border-color,box-shadow] duration-200 hover:border-violet-500 hover:shadow-glow"
      >
        <span className="absolute -left-2 -top-2.5 -rotate-2 bg-ink px-2.5 py-1 font-serif text-[10px] uppercase tracking-caps text-paper">
          Bounty Nº {market.id}
        </span>
        <Stamp
          tone={market.status === "OPEN" ? "violet" : "ink"}
          className="absolute right-3 top-3 rotate-3"
        >
          {market.status}
        </Stamp>
        <h2 className="mb-4 mt-2 pr-16 font-medium leading-snug text-ink">{market.question}</h2>
        <ProbabilityBar outcomes={market.outcomes} />
      </Link>
    </motion.div>
  );
}
