import React from "react"
import { Input } from "@/components/ui/input"
import { cn } from "@/lib/utils"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue
} from "@/components/ui/select"

interface PhoneInputProps {
  countryCode: string
  phoneNumber: string
  onCountryCodeChange: (code: string) => void
  onPhoneNumberChange: (num: string) => void
  error?: string
  disabled?: boolean
  className?: string
}

const COUNTRY_OPTIONS = [
  { code: "+91", label: "🇮🇳 +91" },
  { code: "+1", label: "🇺🇸 +1" },
  { code: "+44", label: "🇬🇧 +44" },
  { code: "+61", label: "🇦🇺 +61" },
  { code: "+81", label: "🇯🇵 +81" }
]

export function PhoneInput({
  countryCode,
  phoneNumber,
  onCountryCodeChange,
  onPhoneNumberChange,
  error,
  disabled = false,
  className
}: PhoneInputProps) {
  const handlePhoneChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const rawVal = e.target.value.replace(/\D/g, "")
    // Limit to standard 10 digit numbers
    onPhoneNumberChange(rawVal.slice(0, 10))
  }

  return (
    <div className={cn("flex flex-col gap-1.5 w-full", className)}>
      <div className="flex gap-2">
        {/* Country Code Select Dropdown */}
        <Select value={countryCode} onValueChange={(val) => onCountryCodeChange(val || "+91")} disabled={disabled}>
          <SelectTrigger className="h-10 w-[110px] rounded-xl border border-border bg-card px-3 text-xs font-bold text-foreground justify-between">
            <SelectValue placeholder="Code" />
          </SelectTrigger>
          <SelectContent className="bg-popover border border-border rounded-xl">
            {COUNTRY_OPTIONS.map((opt) => (
              <SelectItem key={opt.code} value={opt.code}>
                {opt.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

        {/* Real Phone Input */}
        <Input
          type="tel"
          value={phoneNumber}
          onChange={handlePhoneChange}
          placeholder="9876543210"
          disabled={disabled}
          required
          className={cn(
            "flex-1 rounded-xl h-10 text-xs font-semibold focus:ring-2 focus:ring-primary focus:outline-none",
            error && "border-destructive focus:ring-destructive"
          )}
        />
      </div>

      {error && (
        <span className="text-[10px] text-destructive font-semibold mt-0.5">
          {error}
        </span>
      )}
    </div>
  )
}
export default PhoneInput
