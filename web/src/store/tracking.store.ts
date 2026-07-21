import { create } from "zustand"

interface TrackingState {
  latitude: number | null
  longitude: number | null
  etaMins: number | null
  distanceKm: number | null
  connectionStatus: "connected" | "disconnected" | "connecting"
  updateCoordinates: (lat: number, lng: number) => void
  updateEta: (etaMins: number, distanceKm: number) => void
  setConnectionStatus: (connectionStatus: "connected" | "disconnected" | "connecting") => void
  resetTracking: () => void
}

export const useTrackingStore = create<TrackingState>((set) => ({
  latitude: null,
  longitude: null,
  etaMins: null,
  distanceKm: null,
  connectionStatus: "disconnected",
  updateCoordinates: (latitude, longitude) => set({ latitude, longitude }),
  updateEta: (etaMins, distanceKm) => set({ etaMins, distanceKm }),
  setConnectionStatus: (connectionStatus) => set({ connectionStatus }),
  resetTracking: () => set({
    latitude: null,
    longitude: null,
    etaMins: null,
    distanceKm: null,
    connectionStatus: "disconnected"
  })
}))

export default useTrackingStore
