import { useState, useEffect } from "react"

export function useGeolocation() {
  const [latitude, setLatitude] = useState<number | null>(null)
  const [longitude, setLongitude] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [permissionState, setPermissionState] = useState<PermissionState | "unsupported">("prompt")

  const getPosition = () => {
    if (!navigator.geolocation) {
      setError("Geolocation is not supported by this browser.")
      return
    }

    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setLatitude(pos.coords.latitude)
        setLongitude(pos.coords.longitude)
        setError(null)
      },
      (err) => {
        setError(err.message)
      },
      { enableHighAccuracy: true, timeout: 5000, maximumAge: 0 }
    )
  }

  useEffect(() => {
    if (!navigator.permissions || !navigator.permissions.query) {
      setPermissionState("unsupported")
      return
    }

    navigator.permissions.query({ name: "geolocation" as any }).then((result) => {
      setPermissionState(result.state)
      result.onchange = () => {
        setPermissionState(result.state)
      }
    })
  }, [])

  return {
    latitude,
    longitude,
    error,
    permissionState,
    getPosition
  }
}

export default useGeolocation
