"use client";

import { motion, useReducedMotion, useScroll, useTransform } from "motion/react";

export default function LandingWordmark() {
  const reduce = useReducedMotion();
  const { scrollY } = useScroll();
  const parallax = useTransform(scrollY, [0, 600], [0, reduce ? 0 : -90]);

  const lines: Array<{ text: string; className: string; delay: number }> = [
    { text: "Grand Line", className: "[margin-left:-0.05em]", delay: 0.1 },
    { text: "Stonks", className: "[margin-left:12vw]", delay: 0.28 },
  ];

  return (
    <motion.div style={{ y: parallax }}>
      <div className="animate-float">
        <h1 className="font-display text-wordmark uppercase text-violet">
          {lines.map((line) => (
            <span key={line.text} className={`block overflow-hidden whitespace-nowrap ${line.className}`}>
              <motion.span
                className="block"
                initial={{ y: "105%" }}
                animate={{ y: 0 }}
                transition={
                  reduce
                    ? { duration: 0 }
                    : { duration: 0.9, delay: line.delay, ease: [0.16, 1, 0.3, 1] }
                }
              >
                {line.text}
              </motion.span>
            </span>
          ))}
        </h1>
      </div>
    </motion.div>
  );
}
