"use client";

import AuthForm from "@/components/AuthForm";
import { BerryAmount } from "@/components/Berry";
import { useAuth } from "@/lib/auth";

export default function SignupPage() {
  const { signup } = useAuth();
  return (
    <AuthForm
      title="Sign up"
      note={
        <>
          Every new trader starts with{" "}
          <BerryAmount value={10000} className="font-bold text-violet" /> in play-money
          berries.
        </>
      }
      submitLabel="Create account"
      onSubmit={signup}
      newPassword
      altText="Already have an account?"
      altHref="/login"
      altLinkLabel="Log in"
    />
  );
}
