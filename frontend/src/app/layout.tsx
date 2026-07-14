import type { Metadata } from "next";
import { Archivo_Black, Bungee, Fraunces, Inter, Space_Mono } from "next/font/google";
import "./globals.css";
import { AuthProvider } from "@/lib/auth";
import Grain from "@/components/fx/Grain";
import SmoothScroll from "@/components/fx/SmoothScroll";

const display = Archivo_Black({
  weight: "400",
  subsets: ["latin"],
  variable: "--font-display",
});
const impact = Bungee({
  weight: "400",
  subsets: ["latin"],
  variable: "--font-impact",
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
      <body
        className={`${display.variable} ${impact.variable} ${serif.variable} ${sans.variable} ${mono.variable}`}
      >
        <SmoothScroll />
        <Grain />
        <AuthProvider>{children}</AuthProvider>
      </body>
    </html>
  );
}
