import type { Metadata } from "next";
import { Archivo_Black, Fraunces, Inter, Space_Mono } from "next/font/google";
import "./globals.css";
import { AuthProvider } from "@/lib/auth";
import Nav from "@/components/Nav";

const display = Archivo_Black({
  weight: "400",
  subsets: ["latin"],
  variable: "--font-display",
});
const serif = Fraunces({
  subsets: ["latin"],
  variable: "--font-serif",
});
const sans = Inter({
  subsets: ["latin"],
  variable: "--font-sans",
});
const mono = Space_Mono({
  weight: ["400", "700"],
  subsets: ["latin"],
  variable: "--font-mono",
});

export const metadata: Metadata = {
  title: "Grand Line Stonks",
  description: "A play-money prediction market for the Grand Line",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className={`${display.variable} ${serif.variable} ${sans.variable} ${mono.variable}`}>
        <AuthProvider>
          <Nav />
          <main className="mx-auto max-w-5xl px-4 py-8">{children}</main>
        </AuthProvider>
      </body>
    </html>
  );
}
