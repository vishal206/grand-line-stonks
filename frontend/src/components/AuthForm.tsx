"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { ApiError } from "@/lib/api";

const schema = z.object({
  username: z
    .string()
    .min(3, "at least 3 characters")
    .max(50)
    .regex(/^[a-zA-Z0-9_]+$/, "letters, numbers, underscores only"),
  password: z.string().min(8, "at least 8 characters").max(72),
});

type FormValues = z.infer<typeof schema>;

interface AuthFormProps {
  title: string;
  note?: React.ReactNode;
  submitLabel: string;
  onSubmit: (username: string, password: string) => Promise<void>;
  altText: string;
  altHref: string;
  altLinkLabel: string;
  newPassword?: boolean;
}

const inputClass =
  "w-full border border-ink/20 bg-paper px-3 py-2 text-sm text-ink focus:border-violet-vivid focus:outline-none";

export default function AuthForm({
  title,
  note,
  submitLabel,
  onSubmit,
  altText,
  altHref,
  altLinkLabel,
  newPassword = false,
}: AuthFormProps) {
  const router = useRouter();
  const [serverError, setServerError] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ resolver: zodResolver(schema) });

  const submit = handleSubmit(async (values) => {
    setServerError(null);
    try {
      await onSubmit(values.username, values.password);
      router.push("/markets");
    } catch (e) {
      setServerError(e instanceof ApiError ? e.message : "something went wrong");
    }
  });

  return (
    <div className="mx-auto mt-16 max-w-sm border border-ink/10 bg-white p-8">
      <p className="font-serif text-[11px] uppercase tracking-caps text-violet">
        Grand Line Stonks
      </p>
      <h1 className="mb-2 mt-1 text-xl font-semibold text-ink">{title}</h1>
      {note && <p className="mb-4 text-sm text-ink/70">{note}</p>}
      <form onSubmit={submit} className="mt-4 space-y-4">
        <div>
          <label htmlFor="username" className="mb-1.5 block text-sm text-ink/70">
            Username
          </label>
          <input id="username" autoComplete="username" {...register("username")} className={inputClass} />
          {errors.username && (
            <p className="mt-1 text-xs text-negative">{errors.username.message}</p>
          )}
        </div>
        <div>
          <label htmlFor="password" className="mb-1.5 block text-sm text-ink/70">
            Password
          </label>
          <input
            id="password"
            type="password"
            autoComplete={newPassword ? "new-password" : "current-password"}
            {...register("password")}
            className={inputClass}
          />
          {errors.password && (
            <p className="mt-1 text-xs text-negative">{errors.password.message}</p>
          )}
        </div>
        {serverError && (
          <p role="alert" className="text-sm text-negative">
            {serverError}
          </p>
        )}
        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full bg-violet-vivid py-2.5 text-sm font-medium text-paper hover:bg-violet disabled:opacity-50"
        >
          {isSubmitting ? "…" : submitLabel}
        </button>
      </form>
      <p className="mt-5 text-center text-sm text-ink/60">
        {altText}{" "}
        <Link href={altHref} className="text-violet underline-offset-2 hover:underline">
          {altLinkLabel}
        </Link>
      </p>
    </div>
  );
}
