export interface AuthResponse {
  token: string;
}

export interface MeResponse {
  userId: number;
  username: string;
  balance: number;
}

export type MarketStatus = "DRAFT" | "OPEN" | "CLOSED" | "RESOLVED" | "SETTLED" | "CANCELLED";

export interface OutcomeResponse {
  id: number;
  idx: number;
  label: string;
  price: number;
  shares: number;
}

export interface MarketResponse {
  id: number;
  question: string;
  status: MarketStatus;
  liquidity: number;
  createdBy: number;
  createdAt: string;
  winningOutcomeId: number | null;
  outcomes: OutcomeResponse[];
}

export interface BetResponse {
  betId: number;
  transactionId: number;
  marketId: number;
  outcomeId: number;
  amount: number;
  shares: number;
  pricesAfter: number[];
}

export interface PricePointResponse {
  timestamp: string;
  prices: number[];
}

export interface PositionResponse {
  positionId: number;
  marketId: number;
  question: string;
  marketStatus: MarketStatus;
  outcomeId: number;
  outcomeLabel: string;
  shares: number;
  spent: number;
  won: boolean;
}

export interface TransactionHistoryResponse {
  transactionId: number;
  type: string;
  amount: number;
  createdAt: string;
}

export interface Page<T> {
  content: T[];
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
}
