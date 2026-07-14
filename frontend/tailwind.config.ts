import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        paper: "#FAF8F2",
        cloud: "#EFEBE1",
        ink: "#161020",
        violet: {
          DEFAULT: "#5B21B6",
          900: "#3B1477",
          700: "#5B21B6",
          500: "#7C3AED",
          300: "#A78BFA",
          vivid: "#7C3AED",
        },
        lilac: "#EAE2FB",
        pink: "#EC4899",
        positive: "#2E7D5B",
        negative: "#B23A48",
      },
      fontFamily: {
        display: ["var(--font-display)", "sans-serif"],
        impact: ["var(--font-impact)", "sans-serif"],
        serif: ["var(--font-serif)", "serif"],
        sans: ["var(--font-sans)", "system-ui", "sans-serif"],
        mono: ["var(--font-mono)", "monospace"],
      },
      fontSize: {
        wordmark: ["clamp(4.5rem, 17vw, 21rem)", { lineHeight: "0.82" }],
        "display-lg": ["clamp(3rem, 9vw, 8rem)", { lineHeight: "0.9" }],
        "display-md": ["clamp(2rem, 5vw, 4rem)", { lineHeight: "0.95" }],
      },
      letterSpacing: {
        caps: "0.18em",
      },
      boxShadow: {
        glow: "0 0 24px rgba(124, 58, 237, 0.35)",
        "glow-sm": "0 0 12px rgba(124, 58, 237, 0.3)",
        lift: "0 12px 32px -8px rgba(59, 20, 119, 0.25)",
      },
      backgroundImage: {
        "cel-gradient":
          "linear-gradient(135deg, #3B1477 0%, #3B1477 45%, #5B21B6 45%, #5B21B6 72%, #7C3AED 72%, #7C3AED 100%)",
      },
      keyframes: {
        drift: {
          "0%, 100%": { transform: "translate3d(-6%, 0, 0) scale(1)" },
          "50%": { transform: "translate3d(6%, -5%, 0) scale(1.12)" },
        },
        marquee: {
          "0%": { transform: "translateX(0)" },
          "100%": { transform: "translateX(-50%)" },
        },
        float: {
          "0%, 100%": { transform: "translateY(0)" },
          "50%": { transform: "translateY(-10px)" },
        },
        "pulse-glow": {
          "0%, 100%": { opacity: "0.35" },
          "50%": { opacity: "0.9" },
        },
        "pulse-dot": {
          "0%, 100%": { transform: "scale(1)", opacity: "1" },
          "50%": { transform: "scale(1.6)", opacity: "0.5" },
        },
      },
      animation: {
        drift: "drift 16s ease-in-out infinite",
        "drift-slow": "drift 26s ease-in-out infinite",
        marquee: "marquee 40s linear infinite",
        float: "float 7s ease-in-out infinite",
        "pulse-glow": "pulse-glow 2.4s ease-in-out infinite",
        "pulse-dot": "pulse-dot 1.8s ease-in-out infinite",
      },
    },
  },
  plugins: [],
};
export default config;
