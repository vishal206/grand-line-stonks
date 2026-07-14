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
        paper: "#FAFAF7",
        cloud: "#F1EFEA",
        ink: "#17141F",
        violet: {
          DEFAULT: "#4C1D95",
          vivid: "#6D28D9",
        },
        lilac: "#EDE7F6",
        positive: "#2E7D5B",
        negative: "#B23A48",
      },
      fontFamily: {
        display: ["var(--font-display)", "sans-serif"],
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
      keyframes: {
        drift: {
          "0%, 100%": { transform: "translate3d(-6%, 0, 0) scale(1)" },
          "50%": { transform: "translate3d(6%, -5%, 0) scale(1.12)" },
        },
      },
      animation: {
        drift: "drift 16s ease-in-out infinite",
        "drift-slow": "drift 26s ease-in-out infinite",
      },
    },
  },
  plugins: [],
};
export default config;
