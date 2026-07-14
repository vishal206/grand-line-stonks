"use client";

import { useState } from "react";
import Link from "next/link";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { ApiError, apiFetch } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { outcomeColor } from "@/lib/palette";
import { Berry, BerryAmount } from "@/components/Berry";
import type { BetResponse, MarketResponse } from "@/lib/types";

const schema = z.object({
  outcomeId: z.coerce.number({ message: "pick an outcome" }),
  amount: z.coerce
    .number({ message: "enter an amount" })
    .positive("amount must be positive")
    .multipleOf(0.0001, "at most 4 decimal places"),
});

interface BetFormProps {
  market: MarketResponse;
  onPlaced: () => void;
}

const heading = "mb-4 font-serif text-xs uppercase tracking-caps text-violet";
const card = "border border-ink/10 bg-white p-5";

export default function BetForm({ market, onPlaced }: BetFormProps) {
  const { me, loading, refreshMe } = useAuth();
  const [serverError, setServerError] = useState<string | null>(null);
  const [lastBet, setLastBet] = useState<BetResponse | null>(null);
  const [idempotencyKey, setIdempotencyKey] = useState(() => crypto.randomUUID());
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm({ resolver: zodResolver(schema) });

  const submit = handleSubmit(async (values) => {
    setServerError(null);
    try {
      const bet = await apiFetch<BetResponse>(`/api/markets/${market.id}/bets`, {
        method: "POST",
        body: JSON.stringify({ ...values, idempotencyKey }),
      });
      setLastBet(bet);
      setIdempotencyKey(crypto.randomUUID());
      reset();
      await refreshMe();
      onPlaced();
    } catch (e) {
      setServerError(e instanceof ApiError ? e.message : "something went wrong");
    }
  });

  if (!loading && !me) {
    return (
      <div className={card}>
        <h3 className={heading}>Place a bet</h3>
        <p className="mb-4 text-sm text-ink/70">Log in to bet on this market.</p>
        <Link
          href="/login"
          className="block w-full bg-violet-vivid py-2.5 text-center text-sm font-medium text-paper hover:bg-violet"
        >
          Log in
        </Link>
        <p className="mt-3 text-center text-sm text-ink/60">
          New here?{" "}
          <Link href="/signup" className="text-violet underline-offset-2 hover:underline">
            Sign up
          </Link>
        </p>
      </div>
    );
  }

  return (
    <form onSubmit={submit} onChange={() => setServerError(null)} className={card}>
      <h3 className={heading}>Place a bet</h3>
      <fieldset className="mb-4 space-y-2.5">
        <legend className="sr-only">Outcome</legend>
        {market.outcomes.map((outcome) => (
          <label key={outcome.id} className="flex items-center gap-2.5 text-sm text-ink">
            <input
              type="radio"
              value={outcome.id}
              {...register("outcomeId")}
              className="accent-[#6D28D9]"
            />
            <span
              className="inline-block h-2 w-2"
              style={{ backgroundColor: outcomeColor(outcome.idx) }}
            />
            {outcome.label}
            <span className="ml-auto font-mono text-sm tabular-nums text-ink/70">
              {(outcome.price * 100).toFixed(1)}%
            </span>
          </label>
        ))}
      </fieldset>
      {errors.outcomeId && (
        <p className="mb-3 text-xs text-negative">{errors.outcomeId.message}</p>
      )}
      <label htmlFor="amount" className="mb-1.5 flex items-baseline gap-1 text-sm text-ink/70">
        Amount <Berry className="h-[0.7em] w-auto" />
      </label>
      <input
        id="amount"
        inputMode="decimal"
        placeholder="50"
        {...register("amount")}
        className="mb-3 w-full border border-ink/20 bg-paper px-3 py-2 font-mono text-sm text-ink placeholder:text-ink/40 focus:border-violet-vivid focus:outline-none"
      />
      {errors.amount && <p className="mb-3 text-xs text-negative">{errors.amount.message}</p>}
      {serverError && (
        <p role="alert" className="mb-3 text-sm text-negative">
          {serverError}
        </p>
      )}
      {lastBet && !serverError && (
        <p className="mb-3 text-sm text-positive">
          Bought <span className="font-mono">{lastBet.shares.toFixed(4)}</span> shares for{" "}
          <BerryAmount value={lastBet.amount} decimals={4} />
        </p>
      )}
      <button
        type="submit"
        disabled={isSubmitting || market.status !== "OPEN"}
        className="w-full bg-violet-vivid py-2.5 text-sm font-medium text-paper hover:bg-violet disabled:opacity-50"
      >
        {market.status === "OPEN" ? (isSubmitting ? "Placing…" : "Place bet") : "Market not open"}
      </button>
    </form>
  );
}
