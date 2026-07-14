"use client";

import { useState } from "react";
import Link from "next/link";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { ApiError, apiFetch } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { outcomeColor } from "@/lib/palette";
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
      <div className="rounded-lg border border-violet-100 bg-white p-4 shadow-sm">
        <h3 className="mb-3 font-semibold text-gray-900">Place a bet</h3>
        <p className="mb-3 text-sm text-gray-600">Log in to bet on this market.</p>
        <Link
          href="/login"
          className="block w-full rounded bg-violet-600 py-2 text-center text-sm font-medium text-white hover:bg-violet-700"
        >
          Log in
        </Link>
        <p className="mt-2 text-center text-sm text-gray-600">
          New here?{" "}
          <Link href="/signup" className="text-violet-700 hover:underline">
            Sign up
          </Link>
        </p>
      </div>
    );
  }

  return (
    <form
      onSubmit={submit}
      onChange={() => setServerError(null)}
      className="rounded-lg border border-violet-100 bg-white p-4 shadow-sm"
    >
      <h3 className="mb-3 font-semibold text-gray-900">Place a bet</h3>
      <fieldset className="mb-3 space-y-2">
        {market.outcomes.map((outcome) => (
          <label key={outcome.id} className="flex items-center gap-2 text-sm text-gray-700">
            <input
              type="radio"
              value={outcome.id}
              {...register("outcomeId")}
              className="accent-violet-600"
            />
            <span
              className="inline-block h-2 w-2 rounded-full"
              style={{ backgroundColor: outcomeColor(outcome.idx) }}
            />
            {outcome.label}
            <span className="ml-auto text-gray-500">{(outcome.price * 100).toFixed(1)}%</span>
          </label>
        ))}
      </fieldset>
      {errors.outcomeId && <p className="mb-2 text-xs text-red-600">{errors.outcomeId.message}</p>}
      <label htmlFor="amount" className="mb-1 block text-sm text-gray-600">
        Amount (berries)
      </label>
      <input
        id="amount"
        inputMode="decimal"
        placeholder="50"
        {...register("amount")}
        className="mb-2 w-full rounded border border-gray-300 px-3 py-2 text-sm focus:border-violet-500 focus:outline-none"
      />
      {errors.amount && <p className="mb-2 text-xs text-red-600">{errors.amount.message}</p>}
      {serverError && <p className="mb-2 text-sm text-red-600">{serverError}</p>}
      {lastBet && !serverError && (
        <p className="mb-2 text-sm text-emerald-700">
          Bought {lastBet.shares.toFixed(4)} shares for {lastBet.amount} ฿
        </p>
      )}
      <button
        type="submit"
        disabled={isSubmitting || market.status !== "OPEN"}
        className="w-full rounded bg-violet-600 py-2 text-sm font-medium text-white hover:bg-violet-700 disabled:opacity-50"
      >
        {market.status === "OPEN" ? (isSubmitting ? "Placing…" : "Bet") : "Market not open"}
      </button>
    </form>
  );
}
