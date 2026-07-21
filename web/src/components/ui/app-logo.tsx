import { forwardRef } from "react";
import { motion } from "framer-motion";
import { cn } from "@/lib/utils";

export type AppLogoVariant = "full" | "icon" | "wordmark";
export type AppLogoSize = "sm" | "md" | "lg" | "xl";

export interface AppLogoProps extends Omit<React.HTMLAttributes<HTMLDivElement>, "children"> {
  /** "full" = mark + wordmark, "icon" = mark only, "wordmark" = text only. Default: "full" */
  variant?: AppLogoVariant;
  /** Preset size, or a numeric pixel height for the mark. Default: "md" */
  size?: AppLogoSize | number;
  /** Collapse the mark to a single flat currentColor fill (no fg/bg contrast detail). For a single-ink watermark, print, or an overlay on a photo. Default: false */
  monochrome?: boolean;
  /** Mount entrance + hover micro-interaction via Framer Motion. Default: true */
  animated?: boolean;
  className?: string;
}

const SIZE_MAP: Record<AppLogoSize, number> = {
  sm: 24,
  md: 32,
  lg: 48,
  xl: 64,
};

const WORDMARK_TEXT_CLASS: Record<AppLogoSize, string> = {
  sm: "text-base",
  md: "text-xl",
  lg: "text-2xl",
  xl: "text-4xl",
};

/**
 * NavAssist logomark — a location pin (the traveler's arrival point) with a
 * compass needle set inside a porthole (navigation / guidance).
 *
 * Fully neutral palette: every shape reads off `--foreground` / `--background`
 * (design.md §2.2), never a hardcoded hex. That means the mark auto-inverts
 * with the theme — near-black ink on light backgrounds, near-white ink on
 * dark ones — with zero extra logic here; changing `.dark` on <html> is
 * enough. The porthole is a true knockout (filled with `background`, not a
 * fixed white), so it keeps reading as a "hole" on any neutral surface, not
 * just pure white/black.
 */
function LogoMark({ size, monochrome }: { size: number; monochrome: boolean }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 48 48"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      aria-hidden="true"
      className="shrink-0"
    >
      <path
        d="M24 4C14.611 4 7 11.611 7 21C7 32.5 24 45 24 45C24 45 41 32.5 41 21C41 11.611 33.389 4 24 4Z"
        className={monochrome ? "fill-current" : "fill-foreground"}
      />
      <circle
        cx="24"
        cy="20"
        r="8"
        className={monochrome ? "fill-current" : "fill-background"}
        fillOpacity={monochrome ? 0.2 : 1}
      />
      <path
        d="M24 14L27.5 22L24 19.8L20.5 22Z"
        className={monochrome ? "fill-current" : "fill-foreground"}
      />
    </svg>
  );
}

/** Neutral wordmark — "Nav" at full foreground contrast, "Assist" a step down in muted-foreground, so there's still typographic rhythm with zero color/hue in the mix. */
function Wordmark({ size }: { size: AppLogoSize }) {
  return (
    <span className={cn("font-extrabold tracking-tight leading-none select-none", WORDMARK_TEXT_CLASS[size])}>
      <span className="text-foreground">Nav</span>
      <span className="text-muted-foreground">Assist</span>
    </span>
  );
}

/**
 * NavAssist app logo — icon mark, wordmark, or both. Neutral black/white,
 * fully theme-adaptive (see LogoMark above).
 *
 * @example
 * <AppLogo />                                   // full lockup, medium
 * <AppLogo variant="icon" size="lg" />           // mark only
 * <AppLogo variant="icon" monochrome />          // single flat ink, e.g. watermark
 * <AppLogo size={28} animated={false} />         // custom pixel size, no motion
 */
export const AppLogo = forwardRef<HTMLDivElement, AppLogoProps>(
  ({ variant = "full", size = "md", monochrome = false, animated = true, className, ...props }, ref) => {
    const pixelSize = typeof size === "number" ? size : SIZE_MAP[size];
    const wordmarkSize: AppLogoSize = typeof size === "number" ? "md" : size;

    const children = (
      <>
        {variant !== "wordmark" && <LogoMark size={pixelSize} monochrome={monochrome} />}
        {variant !== "icon" && <Wordmark size={wordmarkSize} />}
      </>
    );

    const a11yProps =
      variant === "icon" ? { role: "img" as const, "aria-label": "NavAssist" } : {};

    if (!animated) {
      return (
        <div
          ref={ref}
          className={cn("inline-flex items-center gap-2 text-foreground", className)}
          {...a11yProps}
          {...(props as any)}
        >
          {children}
        </div>
      );
    }

    return (
      <motion.div
        ref={ref}
        className={cn("inline-flex items-center gap-2 text-foreground", className)}
        initial={{ opacity: 0, scale: 0.92 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.3, ease: "easeOut" }}
        whileHover={variant !== "wordmark" ? { rotate: [0, -3, 3, 0], transition: { duration: 0.4 } } : undefined}
        {...a11yProps}
        {...(props as any)}
      >
        {children}
      </motion.div>
    );
  }
);

AppLogo.displayName = "AppLogo";

/**
 * Solid-badge variant for contexts that need a filled square and can't read
 * CSS variables — favicon.svg, PWA/app icons, social share previews, browser
 * tab icon. Favicons render outside your app's DOM, so `--foreground` isn't
 * available there; this uses fixed neutral hex instead (near-black badge,
 * white pin) rather than the brand-token approach used everywhere else.
 *
 * @example
 * <AppLogoBadge size={512} />  // export as PNG for app-icon generation
 */
export function AppLogoBadge({ size = 64, className }: { size?: number; className?: string }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 64 64"
      xmlns="http://www.w3.org/2000/svg"
      role="img"
      aria-label="NavAssist"
      className={cn("shrink-0", className)}
    >
      <rect width="64" height="64" rx="16" fill="#0F172A" />
      <g transform="translate(8 8)">
        <path
          d="M24 4C14.611 4 7 11.611 7 21C7 32.5 24 45 24 45C24 45 41 32.5 41 21C41 11.611 33.389 4 24 4Z"
          fill="#F8FAFC"
        />
        <circle cx="24" cy="20" r="8" fill="#0F172A" />
        <path d="M24 14L27.5 22L24 19.8L20.5 22Z" fill="#F8FAFC" />
      </g>
    </svg>
  );
}

export default AppLogo;
