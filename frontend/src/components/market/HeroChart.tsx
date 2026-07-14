"use client";

import { useReducedMotion } from "motion/react";
import { Area, AreaChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import type { PricePointResponse } from "@/lib/types";

const MUTED = "#6E6A75";
const VIOLET = "#7C3AED";

export default function HeroChart({
  history,
  outcomeIdx,
  outcomeLabel,
}: {
  history: PricePointResponse[];
  outcomeIdx: number;
  outcomeLabel: string;
}) {
  const reduce = useReducedMotion();
  if (history.length === 0) {
    return (
      <p className="flex h-full items-center justify-center py-16 text-center font-serif text-xs uppercase tracking-caps text-ink/50">
        No trades logged yet
      </p>
    );
  }
  const data = history.map((point) => ({
    time: new Date(point.timestamp).getTime(),
    value: point.prices[outcomeIdx],
  }));
  const last = data.length - 1;

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const renderDot = (props: any) => {
    const { cx, cy, index } = props;
    if (index !== last || cx == null) return <g key={`d-${index}`} />;
    return (
      <g key={`d-${index}`}>
        <circle
          cx={cx}
          cy={cy}
          r={9}
          fill={VIOLET}
          opacity={0.3}
          className="animate-ping motion-reduce:animate-none"
          style={{ transformBox: "fill-box", transformOrigin: "center" }}
        />
        <circle cx={cx} cy={cy} r={4.5} fill={VIOLET} stroke="#FFFFFF" strokeWidth={2} />
      </g>
    );
  };

  return (
    <ResponsiveContainer width="100%" height={300}>
      <AreaChart data={data} margin={{ top: 12, right: 18, bottom: 0, left: 0 }}>
        <defs>
          <linearGradient id="heroFill" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor={VIOLET} stopOpacity={0.3} />
            <stop offset="100%" stopColor={VIOLET} stopOpacity={0.02} />
          </linearGradient>
        </defs>
        <XAxis
          dataKey="time"
          type="number"
          domain={["dataMin", "dataMax"]}
          scale="time"
          tickFormatter={(value: number) =>
            new Date(value).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })
          }
          tick={{ fontSize: 11, fill: MUTED, fontFamily: "var(--font-mono)" }}
          axisLine={{ stroke: "#ECEAE4" }}
          tickLine={false}
        />
        <YAxis
          domain={[0, 1]}
          ticks={[0, 0.25, 0.5, 0.75, 1]}
          tickFormatter={(value: number) => `${Math.round(value * 100)}%`}
          tick={{ fontSize: 11, fill: MUTED, fontFamily: "var(--font-mono)" }}
          axisLine={false}
          tickLine={false}
          width={40}
        />
        <Tooltip
          labelFormatter={(value) => new Date(Number(value)).toLocaleString()}
          formatter={(value) => [`${(Number(value) * 100).toFixed(1)}%`, outcomeLabel]}
          contentStyle={{
            fontSize: 12,
            fontFamily: "var(--font-mono)",
            border: "1px solid #ECEAE4",
            borderRadius: 0,
            background: "#FFFFFF",
            color: "#161020",
          }}
        />
        <Area
          type="monotone"
          dataKey="value"
          stroke={VIOLET}
          strokeWidth={2.5}
          fill="url(#heroFill)"
          isAnimationActive={!reduce}
          animationDuration={900}
          dot={renderDot}
          activeDot={{ r: 5, strokeWidth: 2, stroke: "#FFFFFF" }}
        />
      </AreaChart>
    </ResponsiveContainer>
  );
}
