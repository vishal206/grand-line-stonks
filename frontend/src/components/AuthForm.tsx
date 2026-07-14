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
  submitLabel: string;
  onSubmit: (username: string, password: string) => Promise<void>;
  altText: string;
  altHref: string;
  altLinkLabel: string;
}

export default function AuthForm({
  title,
  submitLabel,
  onSubmit,
  altText,
  altHref,
  altLinkLabel,
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
      router.push("/");
    } catch (e) {
      setServerError(e instanceof ApiError ? e.message : "something went wrong");
    }
  });

  return (
    <div className="mx-auto mt-12 max-w-sm rounded-lg border border-violet-100 bg-white p-6 shadow-sm">
      <h1 className="mb-6 text-xl font-bold text-gray-900">{title}</h1>
      <form onSubmit={submit} className="space-y-4">
        <div>
          <label htmlFor="username" className="mb-1 block text-sm text-gray-600">
            Username
          </label>
          <input
            id="username"
            {...register("username")}
            className="w-full rounded border border-gray-300 px-3 py-2 text-sm focus:border-violet-500 focus:outline-none"
          />
          {errors.username && (
            <p className="mt-1 text-xs text-red-600">{errors.username.message}</p>
          )}
        </div>
        <div>
          <label htmlFor="password" className="mb-1 block text-sm text-gray-600">
            Password
          </label>
          <input
            id="password"
            type="password"
            {...register("password")}
            className="w-full rounded border border-gray-300 px-3 py-2 text-sm focus:border-violet-500 focus:outline-none"
          />
          {errors.password && (
            <p className="mt-1 text-xs text-red-600">{errors.password.message}</p>
          )}
        </div>
        {serverError && <p className="text-sm text-red-600">{serverError}</p>}
        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full rounded bg-violet-600 py-2 text-sm font-medium text-white hover:bg-violet-700 disabled:opacity-50"
        >
          {isSubmitting ? "…" : submitLabel}
        </button>
      </form>
      <p className="mt-4 text-center text-sm text-gray-500">
        {altText}{" "}
        <Link href={altHref} className="text-violet-600 hover:underline">
          {altLinkLabel}
        </Link>
      </p>
    </div>
  );
}
