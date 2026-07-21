<p align="center">
  <img src="../docs/assets/logo.svg" alt="NavAssist Logo" width="100">
</p>

<h1 align="center">NavAssist Frontend</h1>

<p align="center">
  React Client Application
</p>

---

## 🛠️ Overview

This is the front-end application client for the NavAssist platform, designed as a fast, responsive Single Page Application (SPA). It uses modern React practices and dark-first monochrome visual design parameters.

### Tech Stack:
- **Core Library**: React (v19)
- **Programming Language**: TypeScript
- **Bundler**: Vite
- **Styling**: Tailwind CSS
- **Design Tokens**: shadcn/ui primitives
- **Animations**: Framer Motion
- **State Management**: Zustand, React Context
- **API Client**: Axios

---

## 📁 Project Structure

```
web/
├── src/
│   ├── app/           # App root initialization, styles, and routing patterns
│   ├── components/    # Atomic UI widgets and support controls
│   │   ├── booking/   # Booking detail cards and OTP verifiers
│   │   ├── support/   # ChatGPT style conversation modules
│   │   └── ui/        # Primitives (dialogs, buttons, sheets)
│   ├── hooks/         # Custom React hooks (auth context, geolocation tracker)
│   ├── pages/         # High-level router pages (support thread, dashboard)
│   ├── services/      # Backend API connectors (Axios interceptors)
│   └── store/         # Zustand store objects (trips, layouts)
```

---

## 🎨 UI Features & Design System
- **Adaptable Colors**: Standard Tailwind variables adapt to light or dark modes automatically.
- **ChatGPT Thread Experience**: Shrinks bubbles to fit text size (`w-fit`), hides background boxes for single images, and groups messages by same sender within 5-minute intervals.
- **Mobile Sheet Sidebars**: Desktop grids slide out as side drawers on mobile screens using Sheet dialog components.
- **Micro-Animations**: Framer Motion provides transitions for status changes and messages list additions.

---

## 🚀 Running Frontend Locally

### 1. Install dependencies
```bash
npm install
```

### 2. Start dev server
```bash
npm run dev
```
Open `http://localhost:5173` in your browser.

### 3. Production build
```bash
npm run build
```

---

## ⚙️ Environment Variables

| Variable | Description |
| :--- | :--- |
| `VITE_API_URL` | Live backend API address |
| `VITE_RAZORPAY_KEY` | Razorpay Client Public Key |
| `VITE_GOOGLE_MAPS_KEY` | Google Maps SDK API Key |

---

## 🚀 Production Optimizations
- **Dynamic Chunk Splitting**: Splitting packages so pages load assets dynamically on route change.
- **Axios 1.x Upload Handler**: Correctly clears `Content-Type` headers via `.delete()` to let browsers append boundaries on `FormData` uploads.
