import React, { useRef, useEffect } from "react"
import { cn } from "@/lib/utils"

interface OtpInputProps {
  value: string
  onChange: (val: string) => void
  length?: number
  disabled?: boolean
}

export function OtpInput({ value, onChange, length = 4, disabled = false }: OtpInputProps) {
  const inputsRef = useRef<(HTMLInputElement | null)[]>([])

  const valuesArray = value.split("").slice(0, length)
  // Pad with empty strings if value length is less than expected length
  while (valuesArray.length < length) {
    valuesArray.push("")
  }

  const focusInput = (index: number) => {
    if (inputsRef.current[index]) {
      inputsRef.current[index]?.focus()
      inputsRef.current[index]?.select()
    }
  }

  const handleOtpChange = (index: number, digit: string) => {
    // Only accept single digit
    const cleanedDigit = digit.replace(/\D/g, "")
    const newValues = [...valuesArray]
    newValues[index] = cleanedDigit ? cleanedDigit[cleanedDigit.length - 1] : ""
    const newValString = newValues.join("")
    onChange(newValString)

    // Auto advance focus
    if (cleanedDigit && index < length - 1) {
      focusInput(index + 1)
    }
  }

  const handleKeyDown = (index: number, e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Backspace") {
      if (!valuesArray[index] && index > 0) {
        // Go back if current cell is empty
        const newValues = [...valuesArray]
        newValues[index - 1] = ""
        onChange(newValues.join(""))
        focusInput(index - 1)
      } else {
        const newValues = [...valuesArray]
        newValues[index] = ""
        onChange(newValues.join(""))
      }
    } else if (e.key === "ArrowLeft" && index > 0) {
      focusInput(index - 1)
    } else if (e.key === "ArrowRight" && index < length - 1) {
      focusInput(index + 1)
    }
  }

  const handlePaste = (e: React.ClipboardEvent<HTMLInputElement>) => {
    e.preventDefault()
    const pasteData = e.clipboardData.getData("text").replace(/\D/g, "").slice(0, length)
    onChange(pasteData)
    
    // Focus last filled index or fallback to the last cell
    const targetFocus = Math.min(pasteData.length, length - 1)
    focusInput(targetFocus)
  }

  // Synchronize inputsRef array size
  useEffect(() => {
    inputsRef.current = inputsRef.current.slice(0, length)
  }, [length])

  return (
    <div className="flex gap-3.5 justify-center items-center">
      {Array.from({ length }).map((_, idx) => (
        <input
          key={idx}
          type="text"
          inputMode="numeric"
          pattern="[0-9]*"
          maxLength={1}
          disabled={disabled}
          value={valuesArray[idx]}
          ref={(el) => { inputsRef.current[idx] = el }}
          onChange={(e) => handleOtpChange(idx, e.target.value)}
          onKeyDown={(e) => handleKeyDown(idx, e)}
          onPaste={handlePaste}
          onFocus={() => focusInput(idx)}
          className={cn(
            "w-12 h-14 text-center text-lg font-black rounded-xl border border-border bg-card text-foreground focus:ring-2 focus:ring-primary focus:border-primary focus:outline-none transition-all",
            disabled && "opacity-50 pointer-events-none bg-muted"
          )}
        />
      ))}
    </div>
  )
}
export default OtpInput
