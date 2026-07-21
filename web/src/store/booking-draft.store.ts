import { create } from "zustand"
import type { AssistantProfile } from "@/types/assistant.types"
import type { Coupon } from "@/types/payment.types"

interface LocationPoint {
  name: string
  lat: number
  lng: number
}

interface BookingDraftState {
  pickup: LocationPoint | null
  destination: LocationPoint | null
  schedule: string | null
  selectedAssistant: AssistantProfile | null
  coupon: Coupon | null
  setPickup: (pickup: LocationPoint | null) => void
  setDestination: (destination: LocationPoint | null) => void
  setSchedule: (schedule: string | null) => void
  setSelectedAssistant: (assistant: AssistantProfile | null) => void
  setCoupon: (coupon: Coupon | null) => void
  resetDraft: () => void
}

export const useBookingDraftStore = create<BookingDraftState>((set) => ({
  pickup: null,
  destination: null,
  schedule: null,
  selectedAssistant: null,
  coupon: null,
  setPickup: (pickup) => set({ pickup }),
  setDestination: (destination) => set({ destination }),
  setSchedule: (schedule) => set({ schedule }),
  setSelectedAssistant: (selectedAssistant) => set({ selectedAssistant }),
  setCoupon: (coupon) => set({ coupon }),
  resetDraft: () => set({
    pickup: null,
    destination: null,
    schedule: null,
    selectedAssistant: null,
    coupon: null
  })
}))

export default useBookingDraftStore
