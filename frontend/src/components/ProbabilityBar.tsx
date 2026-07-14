import { outcomeColor } from "@/lib/palette";
import type { OutcomeResponse } from "@/lib/types";

export default function ProbabilityBar({ outcomes }: { outcomes: OutcomeResponse[] }) {
  return (
    <div>
      <div className="flex h-1.5 w-full gap-[2px] overflow-hidden">
        {outcomes.map((outcome) => (
          <div
            key={outcome.id}
            style={{
              width: `${Math.max(outcome.price * 100, 1)}%`,
              backgroundColor: outcomeColor(outcome.idx),
            }}
          />
        ))}
      </div>
      <div className="mt-2.5 flex flex-wrap gap-x-5 gap-y-1.5">
        {outcomes.map((outcome) => (
          <span key={outcome.id} className="flex items-baseline gap-1.5 text-xs text-ink/70">
            <span
              className="inline-block h-2 w-2 self-center"
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
