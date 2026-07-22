<p align="center">
  <h1 align="center">NavAssist Android Application</h1>
  <p align="center">
    <strong>Enterprise-Grade Android Client for Travelers, Assistants, and Operations Admins</strong>
  </p>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android Badge">
  <img src="https://img.shields.io/badge/Language-Kotlin_1.9-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin Badge">
  <img src="https://img.shields.io/badge/Architecture-MVVM_%2B_Clean-005571?style=for-the-badge" alt="Architecture Badge">
  <img src="https://img.shields.io/badge/DI-Hilt-4285F4?style=for-the-badge&logo=google&logoColor=white" alt="Hilt Badge">
  <img src="https://img.shields.io/badge/Design-Material_3-757575?style=for-the-badge&logo=materialdesign&logoColor=white" alt="Material 3 Badge">
  <img src="https://img.shields.io/badge/Maps-MapLibre_11.x-000000?style=for-the-badge&logo=maplibre&logoColor=white" alt="MapLibre Badge">
  <img src="https://img.shields.io/badge/Payments-Razorpay-02042B?style=for-the-badge" alt="Razorpay Badge">
</p>

---

## 📖 Overview

The **NavAssist Android Application** is a native, high-performance Android mobile application built with **Kotlin, MVVM, Hilt, ViewBinding, and Material 3 XML**. It provides a unified single-activity application supporting all three platform personas:

1. 🧳 **Passenger / Guest**: Search local transit hubs, select pickup/destination points on interactive maps, reserve verified guides, track live movement, manage virtual wallets, process Razorpay payments, and trigger emergency SOS alerts.
2. 🦺 **Assistant / Guide**: Toggle real-time online availability, accept/decline incoming booking requests, navigate turn-by-turn routes, view today's earnings and payout history, manage KYC verification documents, and manage working schedules.
3. 🛡️ **Admin Operations Console**: Monitor real-time SOS emergency alerts, conduct 360° user profile audits, perform booking lifecycle management, and inspect KYC verification document queues.

---

## 🎨 Design System & Aesthetics

Built adhering strictly to modern enterprise aesthetic standards:
- **Background Palette**: Deep Zinc Dark (`#09090B`) with surface card containers (`#18181B`) and ambient glow backdrops (`#111113`).
- **Accent Palette**: Active Emerald (`#22C55E`), Warning Amber (`#F59E0B`), and Emergency Crimson (`#EF4444`).
- **Typography & Components**: Material 3 XML layouts with custom card widgets, smooth shimmer skeletons, and interactive state animations.

---

## 🏛️ Architecture & Tech Stack

```
               ┌──────────────────────────────────────────────┐
               │              MainActivity                    │
               │   (Single Activity + Bottom Navigation)      │
               └──────────────────────┬───────────────────────┘
                                      │ Jetpack Navigation Component
               ┌──────────────────────▼───────────────────────┐
               │              Presentation Layer              │
               │   (BaseFragment, ViewModel, ViewBinding)     │
               └──────────────────────┬───────────────────────┘
                                      │ Kotlin Flows & UiState
               ┌──────────────────────▼───────────────────────┐
               │                Domain Layer                  │
               │    (Use Cases, Repositories, Domain Models)  │
               └──────────────────────┬───────────────────────┘
                                      │ Coroutines & Result<T>
               ┌──────────────────────▼───────────────────────┐
               │                 Data Layer                   │
               │  (Retrofit, OkHttp, DataStore, MapLibre SDK) │
               └──────────────────────────────────────────────┘
```

| Layer | Component / Technology |
|---|---|
| **Architecture** | Single Activity Architecture + Clean Architecture + MVVM Pattern |
| **Dependency Injection** | Hilt (`@HiltAndroidApp`, `@HiltViewModel`, `@AndroidEntryPoint`) |
| **Navigation** | Jetpack Navigation Component (`nav_graph.xml` with SafeArgs) |
| **Asynchronous & Reactive** | Kotlin Coroutines, StateFlow, SharedFlow, repeatOnLifecycle |
| **Networking** | Retrofit 2, OkHttp 4, Auth Interceptors, KotlinX Serialization |
| **Maps & Location** | MapLibre Native SDK (11.5.1), FusedLocationProviderClient |
| **UI Components** | Material 3 XML, ViewBinding, CoordinatorLayout, ConstraintLayout |
| **Storage & Preferences** | Jetpack DataStore (SessionManager & Encrypted Tokens) |
| **Payments** | Razorpay Android Checkout SDK (`1.6.40`) |
| **Image Loading** | Coil (`io.coil-kt:coil`) |

---

## 📂 Project Directory Structure

```
frontend/app/src/main/
├── java/com/navassist/android/
│   ├── NavAssistApplication.kt      # Application class (@HiltAndroidApp)
│   ├── core/                        # Core utilities (network, session, logger)
│   ├── data/                        # Data layer (API interfaces, DTOs, RepositoryImpls)
│   ├── di/                          # Hilt Dependency Injection modules
│   ├── domain/                      # Domain models and repository contracts
│   └── presentation/                # UI Presentation Layer
│       ├── MainActivity.kt          # Host activity with Navigation Controller
│       ├── about/                   # About & App Update fragments
│       ├── admin/                   # Admin Operations Console & Audit screens
│       │   ├── bookings/            # Admin booking management & detail
│       │   ├── kyc/                 # Admin KYC queue & document verification
│       │   ├── sos/                 # Admin live SOS emergency monitor
│       │   └── users/               # Admin user management & 360 detail
│       ├── assistant/               # Assistant / Guide screens
│       │   ├── booking/             # Booking request bottom sheet & accept flow
│       │   ├── earnings/            # Earnings dashboard, payouts & wallet
│       │   ├── home/                # Assistant Dashboard, widgets & adapters
│       │   └── kyc/                 # KYC document upload & status tracking
│       ├── auth/                    # Auth screens (Login, Register, OTP, Reset)
│       ├── booking/                 # Passenger booking flow fragments
│       ├── chat/                    # In-journey chat & voice call screens
│       ├── emergency/               # Emergency contacts management
│       ├── history/                 # Trip history & receipt fragments
│       ├── home/                    # Passenger Home Dashboard & map search
│       ├── journey/                 # Live tracking & turn-by-turn navigation
│       ├── legal/                   # Terms of Service & Privacy Policy
│       ├── notifications/           # In-app notifications center
│       ├── offers/                  # Coupons & promotional offers
│       ├── onboarding/              # Splash, Onboarding ViewPager & Intro
│       ├── payment/                 # Payment processing, success & failed
│       ├── profile/                 # User profile & saved locations
│       ├── rating/                  # Trip rating & review submission
│       ├── referral/                # Referral program & rewards
│       ├── settings/                # Security, privacy, language & settings
│       ├── sos/                     # Emergency SOS panic trigger & tracking
│       ├── support/                 # Support tickets, FAQ & lost & found
│       └── wallet/                  # Internal wallet passbook & top-up
└── res/
    ├── drawable/                    # Custom vectors, ambient glows & rounded backgrounds
    ├── layout/                      # Clean XML layouts with Material 3 components
    ├── menu/                        # Bottom navigation menu XML
    ├── navigation/                  # Single nav_graph.xml with 65+ destinations
    └── values/                      # Colors, dimensions, themes & string resources
```

---

## 🛠️ Requirements & Setup

### Prerequisites
- **Android Studio**: Jellyfish (2023.3.1) or newer
- **JDK**: Java 17+
- **Android SDK**: Min SDK 24 (Android 7.0), Target SDK 34 (Android 14)
- **Gradle**: 8.x+ (Gradle Wrapper included)

### Build Steps

1. **Clone & Navigate**:
   ```bash
   cd NavAssist/frontend
   ```

2. **Sync Gradle**:
   Open the `frontend` folder in Android Studio and run a Gradle Sync.

3. **Build Assembly**:
   ```bash
   ./gradlew :app:assembleDevDebug
   ```

4. **Install & Run on Device/Emulator**:
   ```bash
   adb install -r app/build/outputs/apk/dev/debug/app-dev-debug.apk
   ```

---

## 📄 License

This repository is part of the NavAssist Platform and is released under the **MIT License**.
