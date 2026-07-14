"use client";

import { createContext, useCallback, useContext, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { apiFetch, getToken, setToken } from "./api";
import Loading from "@/components/Loading";
import type { AuthResponse, MeResponse } from "./types";

interface AuthContextValue {
  me: MeResponse | null;
  loading: boolean;
  login: (username: string, password: string) => Promise<void>;
  signup: (username: string, password: string) => Promise<void>;
  logout: () => void;
  refreshMe: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [me, setMe] = useState<MeResponse | null>(null);
  const [loading, setLoading] = useState(true);

  const refreshMe = useCallback(async () => {
    if (!getToken()) {
      setMe(null);
      return;
    }
    try {
      setMe(await apiFetch<MeResponse>("/api/me"));
    } catch {
      setToken(null);
      setMe(null);
    }
  }, []);

  useEffect(() => {
    refreshMe().finally(() => setLoading(false));
  }, [refreshMe]);

  const authenticate = useCallback(
    async (path: string, username: string, password: string) => {
      const response = await apiFetch<AuthResponse>(path, {
        method: "POST",
        body: JSON.stringify({ username, password }),
      });
      setToken(response.token);
      await refreshMe();
    },
    [refreshMe],
  );

  const login = useCallback(
    (username: string, password: string) => authenticate("/api/auth/login", username, password),
    [authenticate],
  );
  const signup = useCallback(
    (username: string, password: string) => authenticate("/api/auth/signup", username, password),
    [authenticate],
  );
  const logout = useCallback(() => {
    setToken(null);
    setMe(null);
  }, []);

  return (
    <AuthContext.Provider value={{ me, loading, login, signup, logout, refreshMe }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used inside AuthProvider");
  return context;
}

export function RequireAuth({ children }: { children: React.ReactNode }) {
  const { me, loading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!loading && !me) {
      router.replace("/login");
    }
  }, [loading, me, router]);

  if (loading || !me) {
    return <Loading label="Checking your papers" className="h-64" />;
  }
  return <>{children}</>;
}
