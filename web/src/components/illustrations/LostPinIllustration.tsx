import { memo, useEffect, useRef } from "react"
import { motion, useAnimate, useReducedMotion } from "framer-motion"

interface LostPinIllustrationProps {
  /** Optional class name applied to the root <svg>, e.g. for layout sizing from the parent. */
  className?: string
  /**
   * Render size in pixels (square). The parent controls responsive sizing via its own
   * wrapper — this only sets the SVG's intrinsic width/height attributes.
   */
  size?: number
}

const ROUTE_PATH_D = "M20 200C80 200 90 140 150 130C200 122 190 80 250 70"
const PIN_BODY_PATH_D =
  "M24 4C14.611 4 7 11.611 7 21C7 32.5 24 45 24 45C24 45 41 32.5 41 21C41 11.611 33.389 4 24 4Z"

const PIN_REST_ROTATE = -4
const RING_COUNT = 3

/**
 * Single source of truth for the choreography's timing. Keeping every duration,
 * delay, and stagger here — rather than scattered through JSX — is what makes a
 * multi-stage animation like this tunable without hunting through markup.
 */
const TIMELINE = {
  routeDraw: 1.2,
  routeDrawReduced: 0.6,
  pinArrive: 0.5,
  pinIdle: 4,
  ringDuration: 2,
  ringStagger: 0.4,
  ringRepeatDelay: 0.4,
} as const

/**
 * Decorative "lost pin" illustration for the 404 page.
 *
 * Choreography (skipped entirely under prefers-reduced-motion, which instead
 * renders every element at its resting state with a single fast path reveal):
 *   1. The dashed route draws itself in once.
 *   2. The pin drops into place at the route's end.
 *   3. Three ping rings pulse beneath the pin, staggered.
 *   4. The pin settles into a slow infinite idle float.
 *
 * Stages 1-2 are strictly sequential (the pin shouldn't appear to "arrive"
 * before the route reaches it), so this uses Framer Motion's imperative
 * `useAnimate` timeline rather than declarative `animate` props — the
 * documented approach for orchestrating dependent, multi-element sequences
 * that can't be expressed as a single keyframe list. Each element still
 * declares its own resting `initial` state, so the component renders
 * correctly even before the effect runs (e.g. under SSR).
 *
 * Purely decorative (aria-hidden) — the page's heading and body copy carry
 * the meaning.
 */
function LostPinIllustrationImpl({
  className,
  size = 320,
}: LostPinIllustrationProps) {
  const shouldReduceMotion = useReducedMotion()
  const [scope, animate] = useAnimate()

  const routeRef = useRef<SVGPathElement>(null)
  const pinRef = useRef<SVGGElement>(null)
  const ringRefs = useRef<(SVGCircleElement | null)[]>([])

  useEffect(() => {
    if (shouldReduceMotion) {
      if (routeRef.current) {
        void animate(
          routeRef.current,
          { pathLength: 1 },
          { duration: TIMELINE.routeDrawReduced, ease: "easeInOut" }
        )
      }
      if (pinRef.current) {
        void animate(
          pinRef.current,
          { opacity: 1, scale: 1, rotate: PIN_REST_ROTATE },
          { duration: 0 }
        )
      }
      return
    }

    let cancelled = false

    const runSequence = async () => {
      if (routeRef.current) {
        await animate(
          routeRef.current,
          { pathLength: 1 },
          { duration: TIMELINE.routeDraw, ease: "easeInOut" }
        )
      }
      if (cancelled) return

      if (pinRef.current) {
        await animate(
          pinRef.current,
          { opacity: 1, scale: 1, rotate: PIN_REST_ROTATE },
          { duration: TIMELINE.pinArrive, ease: "easeOut" }
        )
      }
      if (cancelled) return

      // Rings and the idle float both loop forever, so they fire in parallel
      // (not awaited) once the pin has landed.
      ringRefs.current.forEach((ring, i) => {
        if (!ring) return
        void animate(
          ring,
          { scale: 1.8, opacity: 0 },
          {
            delay: i * TIMELINE.ringStagger,
            duration: TIMELINE.ringDuration,
            repeat: Infinity,
            repeatDelay: TIMELINE.ringRepeatDelay,
            ease: "easeOut",
          }
        )
      })

      if (pinRef.current) {
        void animate(
          pinRef.current,
          { rotate: [PIN_REST_ROTATE, 4, PIN_REST_ROTATE], y: [0, -8, 0] },
          { duration: TIMELINE.pinIdle, ease: "easeInOut", repeat: Infinity }
        )
      }
    }

    void runSequence()

    return () => {
      cancelled = true
    }
  }, [shouldReduceMotion, animate])

  return (
    <svg
      ref={scope}
      viewBox="0 0 320 240"
      width={size}
      height={size}
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      aria-hidden="true"
      focusable="false"
      className={className}
    >
      {/* Layer 1 — dashed route path. Draws once, then stays fully drawn. */}
      <motion.path
        ref={routeRef}
        d={ROUTE_PATH_D}
        className="stroke-border"
        strokeWidth={2}
        strokeDasharray="6 6"
        strokeLinecap="round"
        fill="none"
        initial={{ pathLength: 0 }}
      />

      {/* Layer 2 — the lost pin. Positioned so its local (24,24) origin lands
          at the route path's endpoint (250, 70). style.originX/originY
          (Framer Motion's SVG-aware transform-origin) resolve against the
          element's actual bounding box, which avoids the fill-box/view-box
          inconsistency browsers have for plain CSS percentage
          transform-origins on SVG elements. */}
      <motion.g
        ref={pinRef}
        transform="translate(226 30)"
        initial={{ opacity: 0, scale: 0.6, rotate: -15 }}
        style={{
          originX: 0.5,
          originY: 0.5,
          willChange: shouldReduceMotion ? undefined : "transform",
        }}
      >
        <path d={PIN_BODY_PATH_D} className="fill-foreground" />
        <circle cx="24" cy="20" r="8" className="fill-background" />
        {/* Question mark — the one geometry swap from the app's guided-pin
            mark, signaling "lost" rather than "guided". */}
        <text
          x="24"
          y="25"
          textAnchor="middle"
          className="fill-muted-foreground"
          style={{
            fontSize: 13,
            fontWeight: 700,
            fontFamily: "Inter, sans-serif",
          }}
        >
          ?
        </text>
      </motion.g>

      {/* Layer 3 — ping rings, staggered, looping beneath the pin's base point. */}
      {!shouldReduceMotion &&
        Array.from({ length: RING_COUNT }).map((_, i) => (
          <motion.circle
            key={i}
            ref={(el) => {
              ringRefs.current[i] = el
            }}
            cx="250"
            cy="78"
            r={4}
            className="stroke-muted-foreground"
            strokeWidth={1.5}
            fill="none"
            initial={{ scale: 1, opacity: 0.5 }}
            style={{
              originX: 0.5,
              originY: 0.5,
              willChange: "transform, opacity",
            }}
          />
        ))}
    </svg>
  )
}

/**
 * Memoized — this is a decorative, prop-stable illustration that runs its own
 * internal animation timeline, so it never needs to re-render in response to
 * parent re-renders once mounted.
 */
export const LostPinIllustration = memo(LostPinIllustrationImpl)