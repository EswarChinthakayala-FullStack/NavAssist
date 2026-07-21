# NavAssist Frontend Client - React + TypeScript + Vite

This is the front-end client interface for the **NavAssist** platform, built as a modern, high-performance single page application (SPA).

---

## 🛠️ Technology Stack & Styling

- **Build Tool**: Vite (with SWC for lightning-fast compilation)
- **Language**: TypeScript
- **Styling System**: Tailwind CSS (supporting responsive design and smooth Dark/Light mode theme switching)
- **UI Components**: shadcn/ui primitives, Phosphor Icons
- **Animation System**: Framer Motion
- **State Management**: React Context, Zustand state managers

---

## 📂 Core Folder Architecture

All source code is located in the `src/` directory:

```
src/
├── app/          # Global application config and router layout definitions
├── components/   # Modular UI primitives and support feature components
│   ├── booking/  # OTP, ride summary, and timeline widgets
│   ├── support/  # ChatGPT-style chat elements (MessageGroup, ChatMessage, etc.)
│   └── ui/       # Standard shadcn/ui atomic elements
├── hooks/        # Custom state handles (Auth, Geolocation, Socket tracking)
├── pages/        # Main route views (Support detail, settings, trip en-route)
├── services/     # Axios client configuration and backend service endpoints
├── store/        # Zustand state engines for bookings and themes
└── styles/       # Tailwind directive files
```

---

## 🚀 Setup & Launch

1. Install Node.js dependencies:
   ```bash
   npm install
   ```
2. Start the hot-reloading development server:
   ```bash
   npm run dev
   ```
   Open `http://localhost:5173` in your browser.
3. Build for production compilation:
   ```bash
   npm run build
   ```
   The compiled static files will be exported to the `dist/` directory.

---

## 🎨 Design Systems & Conventions

- **Responsive Viewport Layouts**: Uses mobile-first layouts with smooth breakpoints for standard phones, tablets, and 3-column desktop layouts.
- **Theme Variables**: Utilizes Tailwind semantic tokens (`bg-card`, `bg-background`, `border-border`, `text-foreground`) supporting both Light and Dark themes dynamically.
- **Support Chat Flow**: Groups contiguous sender messages within a 5-minute window and utilizes standalone video-aspect preview cards for images to eliminate UI clutter.
