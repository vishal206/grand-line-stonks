// Fixed-order categorical palette, validated for CVD separation and contrast on paper.
export const OUTCOME_COLORS = [
  "#6D28D9",
  "#0D9488",
  "#B45309",
  "#BE185D",
  "#1D4ED8",
  "#4D7C0F",
];

export function outcomeColor(idx: number): string {
  return OUTCOME_COLORS[idx] ?? "#6b7280";
}
