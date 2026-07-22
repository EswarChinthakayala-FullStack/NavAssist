export const BOOKING_STATUS_DETAILS = {
  PENDING: {
    label: "Matching Helper",
    colorClass: "bg-warning/10 text-warning border-warning/20",
    description: "Looking for verified local assistants near your location."
  },
  ACCEPTED: {
    label: "Helper Assigned",
    colorClass: "bg-primary/10 text-primary border-primary/20",
    description: "Your assistant is enroute to receive you."
  },
  STARTED: {
    label: "Escort Enroute",
    colorClass: "bg-success/15 text-success border-success/30 animate-pulse",
    description: "OTP confirmed. Safe escort session active."
  },
  COMPLETED: {
    label: "Completed Safely",
    colorClass: "bg-success/10 text-success border-success/20",
    description: "Arrived at your lodging/destination point successfully."
  },
  CANCELLED: {
    label: "Trip Cancelled",
    colorClass: "bg-destructive/10 text-destructive border-destructive/20",
    description: "This booking request was cancelled."
  }
}

export const SERVICE_POINT_TYPES = {
  AIRPORT: {
    label: "Airport Terminal",
    code: "airport"
  },
  RAILWAY_STATION: {
    label: "Railway Platform",
    code: "railway_station"
  },
  BUS_STAND: {
    label: "Bus Terminal Stand",
    code: "bus_stand"
  }
}

export const ROUTE_PATHS = {
  LANDING: "/",
  LOGIN: "/login",
  SIGNUP: "/signup",
  DASHBOARD: "/dashboard",
  BOOKINGS: "/bookings",
  KYC: "/kyc",
  SAFETY: "/safety",
  SETTINGS: "/settings"
}
