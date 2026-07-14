"use client";

import { useEffect, useRef } from "react";
import { useMotionValue, useReducedMotion, useSpring } from "motion/react";

export default function CountUp({
  value,
  decimals = 0,
  className = "",
}: {
  value: number;
  decimals?: number;
  className?: string;
}) {
  const reduce = useReducedMotion();
  const mv = useMotionValue(reduce ? value : 0);
  const spring = useSpring(mv, { stiffness: 90, damping: 24 });
  const ref = useRef<HTMLSpanElement>(null);

  const format = (v: number) =>
    v.toLocaleString(undefined, {
      minimumFractionDigits: 0,
      maximumFractionDigits: decimals,
    });

  useEffect(() => {
    mv.set(value);
  }, [value, mv]);

  useEffect(() => {
    if (reduce) {
      if (ref.current) ref.current.textContent = format(value);
      return;
    }
    return spring.on("change", (v) => {
      if (ref.current) ref.current.textContent = format(v);
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [spring, reduce, value, decimals]);

  return (
    <span ref={ref} className={`tabular-nums ${className}`}>
      {format(value)}
    </span>
  );
}
