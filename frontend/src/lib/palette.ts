export const OUTCOME_COLORS = [
  "#7c3aed",
  "#0d9488",
  "#ea580c",
  "#db2777",
  "#2563eb",
  "#65a30d",
];

export function outcomeColor(idx: number): string {
  return OUTCOME_COLORS[idx] ?? "#6b7280";
}
