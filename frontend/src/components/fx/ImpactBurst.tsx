"use client";

import { useEffect, useState } from "react";
import { AnimatePresence, motion, useReducedMotion } from "motion/react";
import confetti from "canvas-confetti";
import SpeedLines from "./SpeedLines";

export default function ImpactBurst({ burst }: { burst: number }) {
  const reduce = useReducedMotion();
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    if (!burst || reduce) return;
    setVisible(true);
    confetti({
      particleCount: 90,
      spread: 80,
      startVelocity: 38,
      origin: { y: 0.55 },
      colors: ["#7C3AED", "#A78BFA", "#EAB308", "#EC4899"],
      scalar: 0.9,
    });
    const timer = setTimeout(() => setVisible(false), 900);
    return () => clearTimeout(timer);
  }, [burst, reduce]);

  return (
    <AnimatePresence>
      {visible && (
        <motion.div
          className="pointer-events-none fixed inset-0 z-[80] flex items-center justify-center"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          transition={{ duration: 0.12 }}
        >
          <motion.div
            className="absolute inset-0 bg-white"
            initial={{ opacity: 0.75 }}
            animate={{ opacity: 0 }}
            transition={{ duration: 0.4 }}
          />
          <div className="absolute inset-0 text-violet-500 opacity-40">
            <SpeedLines />
          </div>
          <motion.span
            className="font-impact text-7xl uppercase text-violet drop-shadow-[4px_4px_0_rgba(236,72,153,0.55)] sm:text-8xl"
            initial={{ scale: 0.4, rotate: -14, opacity: 0 }}
            animate={{ scale: 1.1, rotate: -6, opacity: 1 }}
            exit={{ scale: 1.2, opacity: 0 }}
            transition={{ type: "spring", stiffness: 320, damping: 13 }}
          >
            Don!!
          </motion.span>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
