import { outcomeColor } from "@/lib/palette";
import type { OutcomeResponse } from "@/lib/types";

export default function ProbabilityBar({ outcomes }: { outcomes: OutcomeResponse[] }) {
  return (
    <div>
      <div className="flex h-2 w-full gap-[2px] overflow-hidden rounded">
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
      <div className="mt-2 flex flex-wrap gap-x-4 gap-y-1">
        {outcomes.map((outcome) => (
          <span key={outcome.id} className="flex items-center gap-1.5 text-xs text-gray-600">
            <span
              className="inline-block h-2 w-2 rounded-full"
              style={{ backgroundColor: outcomeColor(outcome.idx) }}
            />
            {outcome.label}
            <span className="font-semibold text-gray-900">
              {(outcome.price * 100).toFixed(1)}%
            </span>
          </span>
        ))}
      </div>
    </div>
  );
}
