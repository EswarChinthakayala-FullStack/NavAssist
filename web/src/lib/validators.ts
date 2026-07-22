import { z } from "zod"

export const phoneSchema = z
  .string()
  .min(10, { message: "Phone number must be exactly 10 digits" })
  .max(10, { message: "Phone number must be exactly 10 digits" })
  .regex(/^[0-9]+$/, { message: "Phone number must contain only numeric digits" })

export const emailSchema = z
  .string()
  .email({ message: "Invalid email address format" })

export const otpSchema = z
  .string()
  .min(4, { message: "One-time passcode must be 4 digits" })
  .max(6, { message: "One-time passcode must not exceed 6 digits" })
  .regex(/^[0-9]+$/, { message: "One-time passcode must contain only numeric digits" })

export const signupSchema = z
  .object({
    name: z.string().min(2, { message: "Name must be at least 2 characters long" }),
    email: emailSchema,
    phone: phoneSchema,
    password: z.string().min(8, { message: "Password must be at least 8 characters long" }),
    confirmPassword: z.string(),
    role: z.enum(["guest", "assistant"])
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "Passwords do not match",
    path: ["confirmPassword"]
  })

export const bookingSchema = z.object({
  pickupName: z.string().min(3, { message: "Pickup location must be specified" }),
  pickupLatitude: z.number({ message: "Pickup location latitude coordinates are missing" }),
  pickupLongitude: z.number({ message: "Pickup location longitude coordinates are missing" }),
  dropoffName: z.string().min(3, { message: "Destination location must be specified" }),
  dropoffLatitude: z.number({ message: "Destination latitude coordinates are missing" }),
  dropoffLongitude: z.number({ message: "Destination longitude coordinates are missing" }),
  scheduledTime: z.string().optional(),
  couponCode: z.string().optional()
})
