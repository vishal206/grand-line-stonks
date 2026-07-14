"use client";

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import { apiFetch } from "@/lib/api";
import { RequireAuth, useAuth } from "@/lib/auth";
import { BerryAmount } from "@/components/Berry";
import type { Page, PositionResponse, TransactionHistoryResponse } from "@/lib/types";

const sectionHeading = "mb-3 font-serif text-xs uppercase tracking-caps text-violet";
const emptyText = "py-4 font-serif text-xs uppercase tracking-caps text-ink/50";

function PositionOutcome({ position }: { position: PositionResponse }) {
  if (position.marketStatus === "CANCELLED")
    return <span className="bg-cloud px-2 py-1 text-xs text-ink/70">Refunded</span>;
  if (position.marketStatus === "SETTLED")
    return position.won ? (
      <span className="bg-positive/10 px-2 py-1 text-xs font-medium text-positive">
        Won <BerryAmount value={position.shares} />
      </span>
    ) : (
      <span className="bg-cloud px-2 py-1 text-xs text-ink/70">Lost</span>
    );
  return (
    <span className="bg-lilac px-2 py-1 font-serif text-[10px] uppercase tracking-caps text-ink">
      {position.marketStatus}
    </span>
  );
}

function PositionRow({ position }: { position: PositionResponse }) {
  return (
    <div className="flex items-center justify-between gap-4 border-b border-ink/5 py-3 last:border-b-0">
      <div className="min-w-0">
        <Link
          href={`/markets/${position.marketId}`}
          className="block truncate text-sm font-medium text-ink hover:text-violet"
        >
          {position.question}
        </Link>
        <p className="mt-0.5 text-xs text-ink/60">
          {position.outcomeLabel} ·{" "}
          <span className="font-mono tabular-nums">{position.shares.toFixed(4)}</span> shares ·{" "}
          <BerryAmount value={position.spent} decimals={4} /> spent
        </p>
      </div>
      <PositionOutcome position={position} />
    </div>
  );
}

function Portfolio() {
  const { me } = useAuth();
  const [positions, setPositions] = useState<PositionResponse[] | null>(null);
  const [transactions, setTransactions] = useState<TransactionHistoryResponse[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    try {
      const [positionPage, transactionPage] = await Promise.all([
        apiFetch<Page<PositionResponse>>("/api/me/positions?size=50"),
        apiFetch<Page<TransactionHistoryResponse>>("/api/me/transactions?size=20"),
      ]);
      setPositions(positionPage.content);
      setTransactions(transactionPage.content);
      setError(null);
    } catch {
      setError("could not load your portfolio");
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const open = positions?.filter(
    (p) => p.marketStatus !== "SETTLED" && p.marketStatus !== "CANCELLED",
  );
  const resolved = positions?.filter(
    (p) => p.marketStatus === "SETTLED" || p.marketStatus === "CANCELLED",
  );

  return (
    <div>
      <div className="mb-8">
        <p className="font-serif text-xs uppercase tracking-caps text-ink/50">The war chest</p>
        <h1 className="mt-1 text-2xl font-semibold text-ink">Portfolio</h1>
      </div>
      <div className="mb-6 border border-ink/10 bg-white p-5">
        <p className="font-serif text-xs uppercase tracking-caps text-violet">Balance</p>
        {me ? (
          <BerryAmount value={me.balance} className="mt-1 text-3xl font-bold text-ink" />
        ) : (
          <p className="mt-1 text-3xl font-bold text-ink/40">…</p>
        )}
      </div>
      {error && <p className="mb-6 text-sm text-negative">{error}</p>}
      <div className="grid items-start gap-6 lg:grid-cols-2">
        <section className="border border-ink/10 bg-white p-5">
          <h2 className={sectionHeading}>Open positions</h2>
          {!open ? (
            <p className="text-sm text-ink/60">Loading…</p>
          ) : open.length === 0 ? (
            <p className={emptyText}>No open bounties yet</p>
          ) : (
            open.map((p) => <PositionRow key={p.positionId} position={p} />)
          )}
          <h2 className={`${sectionHeading} mt-8`}>Resolved</h2>
          {!resolved ? (
            <p className="text-sm text-ink/60">Loading…</p>
          ) : resolved.length === 0 ? (
            <p className={emptyText}>Nothing has come ashore yet</p>
          ) : (
            resolved.map((p) => <PositionRow key={p.positionId} position={p} />)
          )}
        </section>
        <section className="border border-ink/10 bg-white p-5">
          <h2 className={sectionHeading}>Recent transactions</h2>
          {!transactions ? (
            <p className="text-sm text-ink/60">Loading…</p>
          ) : transactions.length === 0 ? (
            <p className={emptyText}>The ledger is blank</p>
          ) : (
            <table className="w-full text-sm">
              <thead className="sr-only">
                <tr>
                  <th>Type</th>
                  <th>Amount</th>
                  <th>Date</th>
                </tr>
              </thead>
              <tbody>
                {transactions.map((tx, i) => (
                  <tr
                    key={`${tx.transactionId}-${i}`}
                    className="border-b border-ink/5 last:border-b-0"
                  >
                    <td className="py-2.5 text-ink/70">{tx.type}</td>
                    <td className="py-2.5 text-right">
                      <BerryAmount
                        value={tx.amount}
                        decimals={4}
                        signed
                        className={`font-medium ${tx.amount >= 0 ? "text-positive" : "text-ink"}`}
                      />
                    </td>
                    <td className="py-2.5 pl-4 text-right font-mono text-xs text-ink/50">
                      {new Date(tx.createdAt).toLocaleString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>
      </div>
    </div>
  );
}

export default function PortfolioPage() {
  return (
    <RequireAuth>
      <Portfolio />
    </RequireAuth>
  );
}
