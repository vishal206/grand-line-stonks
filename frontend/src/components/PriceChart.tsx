"use client";

import {
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { outcomeColor } from "@/lib/palette";
import type { OutcomeResponse, PricePointResponse } from "@/lib/types";

interface PriceChartProps {
  history: PricePointResponse[];
  outcomes: OutcomeResponse[];
}

export default function PriceChart({ history, outcomes }: PriceChartProps) {
  if (history.length === 0) {
    return <p className="py-8 text-center text-sm text-gray-500">No trades yet.</p>;
  }
  const data = history.map((point) => {
    const row: Record<string, number | string> = {
      time: new Date(point.timestamp).getTime(),
    };
    outcomes.forEach((outcome, i) => {
      row[outcome.label] = point.prices[i];
    });
    return row;
  });

  return (
    <ResponsiveContainer width="100%" height={260}>
      <LineChart data={data} margin={{ top: 8, right: 16, bottom: 0, left: 0 }}>
        <CartesianGrid stroke="#f3f4f6" vertical={false} />
        <XAxis
          dataKey="time"
          type="number"
          domain={["dataMin", "dataMax"]}
          scale="time"
          tickFormatter={(value: number) =>
            new Date(value).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })
          }
          tick={{ fontSize: 11, fill: "#6b7280" }}
          axisLine={{ stroke: "#e5e7eb" }}
          tickLine={false}
        />
        <YAxis
          domain={[0, 1]}
          tickFormatter={(value: number) => `${Math.round(value * 100)}%`}
          tick={{ fontSize: 11, fill: "#6b7280" }}
          axisLine={false}
          tickLine={false}
          width={40}
        />
        <Tooltip
          labelFormatter={(value) => new Date(Number(value)).toLocaleString()}
          formatter={(value) => `${(Number(value) * 100).toFixed(1)}%`}
          contentStyle={{ fontSize: 12, borderRadius: 6, borderColor: "#e5e7eb" }}
        />
        <Legend wrapperStyle={{ fontSize: 12 }} />
        {outcomes.map((outcome) => (
          <Line
            key={outcome.id}
            type="stepAfter"
            dataKey={outcome.label}
            stroke={outcomeColor(outcome.idx)}
            strokeWidth={2}
            dot={false}
            activeDot={{ r: 4 }}
            isAnimationActive={false}
          />
        ))}
      </LineChart>
    </ResponsiveContainer>
  );
}
