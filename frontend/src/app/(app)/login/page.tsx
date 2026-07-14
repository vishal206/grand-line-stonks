"use client";

import AuthForm from "@/components/AuthForm";
import { useAuth } from "@/lib/auth";

export default function LoginPage() {
  const { login } = useAuth();
  return (
    <AuthForm
      title="Log in"
      submitLabel="Log in"
      onSubmit={login}
      altText="No account yet?"
      altHref="/signup"
      altLinkLabel="Sign up"
    />
  );
}
