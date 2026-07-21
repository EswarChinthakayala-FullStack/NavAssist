import React from "react"
import { motion } from "framer-motion"

export function RideCompletionIllustration() {
  // Particles for confetti/stars
  const particles = Array.from({ length: 12 }, (_, i) => {
    const angle = (i * 360) / 12
    const radius = 60 + Math.random() * 40
    const x = Math.cos((angle * Math.PI) / 180) * radius
    const y = Math.sin((angle * Math.PI) / 180) * radius
    return {
      id: i,
      x,
      y,
      size: Math.random() * 6 + 3,
      delay: Math.random() * 0.5,
      color: i % 3 === 0 ? "#10B981" : i % 3 === 1 ? "#3B82F6" : "#E2E8F0" // Emerald, Blue, Zinc
    }
  })

  return (
    <div className="relative w-48 h-48 mx-auto flex items-center justify-center select-none">
      {/* Background soft glowing orb */}
      <motion.div
        className="absolute w-36 h-36 rounded-full bg-emerald-500/10 dark:bg-emerald-500/5 blur-xl border border-emerald-500/20"
        animate={{
          scale: [0.9, 1.1, 0.9],
          opacity: [0.7, 1, 0.7],
        }}
        transition={{
          duration: 3,
          repeat: Infinity,
          ease: "easeInOut",
        }}
      />

      {/* Expanding Ripple Rings */}
      <motion.div
        className="absolute w-24 h-24 rounded-full border border-emerald-500/30"
        initial={{ scale: 0.8, opacity: 1 }}
        animate={{ scale: 1.6, opacity: 0 }}
        transition={{
          duration: 2,
          repeat: Infinity,
          ease: "easeOut",
        }}
      />
      <motion.div
        className="absolute w-24 h-24 rounded-full border border-primary/20"
        initial={{ scale: 0.8, opacity: 1 }}
        animate={{ scale: 2, opacity: 0 }}
        transition={{
          duration: 2.5,
          delay: 0.8,
          repeat: Infinity,
          ease: "easeOut",
        }}
      />

      {/* Main SVG Graphic Canvas */}
      <svg className="w-full h-full z-10 overflow-visible" viewBox="0 0 200 200">
        {/* Animated Dashed Route Path */}
        <motion.path
          d="M 50,150 Q 80,70 120,120 T 150,50"
          fill="none"
          stroke="url(#route-gradient)"
          strokeWidth="6"
          strokeLinecap="round"
          strokeDasharray="8 8"
          initial={{ strokeDashoffset: 16 }}
          animate={{ strokeDashoffset: -100 }}
          transition={{
            duration: 8,
            repeat: Infinity,
            ease: "linear",
          }}
        />

        {/* Solid drawn background route for contrast */}
        <motion.path
          d="M 50,150 Q 80,70 120,120 T 150,50"
          fill="none"
          stroke="#3F3F46" // Zinc-700
          strokeWidth="6"
          strokeLinecap="round"
          className="opacity-20"
          initial={{ pathLength: 0 }}
          animate={{ pathLength: 1 }}
          transition={{ duration: 1.5, ease: "easeInOut" }}
        />

        {/* Start Point Marker (Pickup) */}
        <motion.circle
          cx="50"
          cy="150"
          r="8"
          fill="#3B82F6" // Blue
          stroke="#FFFFFF"
          strokeWidth="2"
          initial={{ scale: 0 }}
          animate={{ scale: 1 }}
          transition={{ type: "spring", delay: 0.3 }}
        />

        {/* End Point Marker (Destination Flag / Pin) */}
        <g transform="translate(150, 50)">
          {/* Animated Glow Halo */}
          <motion.circle
            cx="0"
            cy="0"
            r="16"
            fill="#10B981"
            className="opacity-20"
            animate={{ scale: [1, 1.4, 1], opacity: [0.2, 0.4, 0.2] }}
            transition={{ duration: 2, repeat: Infinity }}
          />

          {/* Bouncing Pin */}
          <motion.g
            initial={{ y: -50, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{
              type: "spring",
              stiffness: 120,
              damping: 10,
              delay: 0.6,
            }}
          >
            {/* Draw pin shape */}
            <path
              d="M 0,-18 C -7,-18 -12,-13 -12,-6 C -12,2 0,12 0,12 C 0,12 12,2 12,-6 C 12,-13 7,-18 0,-18 Z"
              fill="#10B981" // Emerald success pin
              stroke="#FFFFFF"
              strokeWidth="2"
            />
            {/* Small inner dot */}
            <circle cx="0" cy="-6" r="4.5" fill="#FFFFFF" />
          </motion.g>
        </g>

        {/* Bouncing Vehicle Icon traveling on the path */}
        <motion.g
          initial={{ offsetDistance: "0%" }}
          animate={{ offsetDistance: "100%" }}
          transition={{
            duration: 5,
            repeat: Infinity,
            ease: "easeInOut",
          }}
          style={{
            offsetPath: "path('M 50,150 Q 80,70 120,120 T 150,50')",
            offsetRotate: "auto",
          }}
        >
          {/* Vehicle dot */}
          <circle cx="0" cy="0" r="10" fill="#3B82F6" stroke="#FFFFFF" strokeWidth="2" />
          {/* Inner triangle arrow */}
          <path d="M -3,-4 L 5,0 L -3,4 Z" fill="#FFFFFF" transform="rotate(0)" />
        </motion.g>

        {/* Definitions for Gradients */}
        <defs>
          <linearGradient id="route-gradient" x1="0%" y1="100%" x2="100%" y2="0%">
            <stop offset="0%" stopColor="#3B82F6" /> {/* Blue */}
            <stop offset="100%" stopColor="#10B981" /> {/* Emerald */}
          </linearGradient>
        </defs>
      </svg>

      {/* Floating Confetti Particles exploded around */}
      {particles.map((p) => (
        <motion.div
          key={p.id}
          className="absolute rounded-full"
          style={{
            width: p.size,
            height: p.size,
            backgroundColor: p.color,
            x: 0,
            y: 0,
          }}
          animate={{
            x: p.x,
            y: p.y,
            opacity: [0, 1, 1, 0],
            scale: [0.2, 1, 1, 0.4],
          }}
          transition={{
            duration: 2.2,
            delay: p.delay,
            repeat: Infinity,
            repeatDelay: 0.8,
            ease: "easeOut",
          }}
        />
      ))}
    </div>
  )
}

export default RideCompletionIllustration
