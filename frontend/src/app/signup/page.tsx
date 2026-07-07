"use client";

import AuthForm from "@/components/AuthForm";
import { useAuth } from "@/lib/auth";

export default function SignupPage() {
  const { signup } = useAuth();
  return (
    <AuthForm
      title="Sign up"
      submitLabel="Create account"
      onSubmit={signup}
      altText="Already have an account?"
      altHref="/login"
      altLinkLabel="Log in"
    />
  );
}
