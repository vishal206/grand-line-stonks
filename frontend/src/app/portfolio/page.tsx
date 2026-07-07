"use client";

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import { apiFetch } from "@/lib/api";
import { RequireAuth, useAuth } from "@/lib/auth";
import type { Page, PositionResponse, TransactionHistoryResponse } from "@/lib/types";

function PositionRow({ position }: { position: PositionResponse }) {
  const resolved =
    position.marketStatus === "SETTLED" || position.marketStatus === "CANCELLED";
  return (
    <div className="flex items-center justify-between border-b border-gray-100 py-3 last:border-b-0">
      <div>
        <Link
          href={`/markets/${position.marketId}`}
          className="text-sm font-medium text-gray-900 hover:text-violet-700"
        >
          {position.question}
        </Link>
        <p className="text-xs text-gray-500">
          {position.outcomeLabel} · {position.shares.toFixed(4)} shares ·{" "}
          {position.spent.toFixed(2)} ฿ spent
        </p>
      </div>
      {resolved ? (
        position.marketStatus === "CANCELLED" ? (
          <span className="rounded bg-gray-100 px-2 py-1 text-xs text-gray-600">Refunded</span>
        ) : position.won ? (
          <span className="rounded bg-emerald-50 px-2 py-1 text-xs font-medium text-emerald-700">
            Won {position.shares.toFixed(2)} ฿
          </span>
        ) : (
          <span className="rounded bg-gray-100 px-2 py-1 text-xs text-gray-600">Lost</span>
        )
      ) : (
        <span className="rounded bg-violet-50 px-2 py-1 text-xs text-violet-700">
          {position.marketStatus}
        </span>
      )}
    </div>
  );
}

function Portfolio() {
  const { me } = useAuth();
  const [positions, setPositions] = useState<PositionResponse[] | null>(null);
  const [transactions, setTransactions] = useState<TransactionHistoryResponse[] | null>(null);

  const load = useCallback(async () => {
    const [positionPage, transactionPage] = await Promise.all([
      apiFetch<Page<PositionResponse>>("/api/me/positions?size=50"),
      apiFetch<Page<TransactionHistoryResponse>>("/api/me/transactions?size=20"),
    ]);
    setPositions(positionPage.content);
    setTransactions(transactionPage.content);
  }, []);

  useEffect(() => {
    load().catch(() => {});
  }, [load]);

  const open = positions?.filter(
    (p) => p.marketStatus !== "SETTLED" && p.marketStatus !== "CANCELLED",
  );
  const resolved = positions?.filter(
    (p) => p.marketStatus === "SETTLED" || p.marketStatus === "CANCELLED",
  );

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold text-gray-900">Portfolio</h1>
      <div className="mb-6 rounded-lg border border-violet-100 bg-violet-50 p-4">
        <p className="text-sm text-gray-600">Balance</p>
        <p className="text-3xl font-bold text-violet-700">
          {me ? Number(me.balance).toLocaleString() : "…"} ฿
        </p>
      </div>
      <div className="grid gap-6 lg:grid-cols-2">
        <section className="rounded-lg border border-violet-100 bg-white p-4 shadow-sm">
          <h2 className="mb-2 font-semibold text-gray-900">Open positions</h2>
          {!open ? (
            <p className="text-sm text-gray-500">Loading…</p>
          ) : open.length === 0 ? (
            <p className="text-sm text-gray-500">No open positions.</p>
          ) : (
            open.map((p) => <PositionRow key={p.positionId} position={p} />)
          )}
          <h2 className="mb-2 mt-6 font-semibold text-gray-900">Resolved</h2>
          {!resolved ? (
            <p className="text-sm text-gray-500">Loading…</p>
          ) : resolved.length === 0 ? (
            <p className="text-sm text-gray-500">Nothing resolved yet.</p>
          ) : (
            resolved.map((p) => <PositionRow key={p.positionId} position={p} />)
          )}
        </section>
        <section className="rounded-lg border border-violet-100 bg-white p-4 shadow-sm">
          <h2 className="mb-2 font-semibold text-gray-900">Recent transactions</h2>
          {!transactions ? (
            <p className="text-sm text-gray-500">Loading…</p>
          ) : transactions.length === 0 ? (
            <p className="text-sm text-gray-500">No transactions yet.</p>
          ) : (
            <table className="w-full text-sm">
              <tbody>
                {transactions.map((tx, i) => (
                  <tr key={`${tx.transactionId}-${i}`} className="border-b border-gray-100 last:border-b-0">
                    <td className="py-2 text-gray-600">{tx.type}</td>
                    <td
                      className={`py-2 text-right font-medium ${
                        tx.amount < 0 ? "text-gray-900" : "text-emerald-700"
                      }`}
                    >
                      {tx.amount > 0 ? "+" : ""}
                      {tx.amount.toFixed(4)} ฿
                    </td>
                    <td className="py-2 pl-4 text-right text-xs text-gray-400">
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
