<p align="center">
  <img src="docs/assets/logo.svg" alt="NavAssist Logo" width="120">
</p>

<h1 align="center">NavAssist</h1>

<p align="center">
  <strong>AI-Powered Smart Escort & Travel Assistance Platform</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/FastAPI-005571?style=for-the-badge&logo=fastapi" alt="FastAPI Badge">
  <img src="https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react" alt="React Badge">
  <img src="https://img.shields.io/badge/TypeScript-007ACC?style=for-the-badge&logo=typescript" alt="TypeScript Badge">
  <img src="https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=for-the-badge&logo=tailwind-css" alt="Tailwind Badge">
  <img src="https://img.shields.io/badge/MySQL-00758F?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL Badge">
  <img src="https://img.shields.io/badge/Razorpay-02042B?style=for-the-badge" alt="Razorpay Badge">
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker" alt="Docker Badge">
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge" alt="License Badge">
</p>

---

## 📖 About NavAssist

NavAssist is an enterprise-grade, full-stack customer support and ride assistance management platform. It matches travelers with verified local assistance escorts to help them navigate complex local transit hubs (like railway stations, airports, and bus terminals).

### Core Capabilities:
- **AI-Powered Travel Escort Matching**: Instantly routes and pairs travelers with optimal, nearby support assistants.
- **Live Assistant Geo-Tracking**: Uses real-time geographic telemetry to display en-route paths and live navigation.
- **Strict KYC Onboarding**: Multi-stage documents vetting verification workflow for support desk operators.
- **SOS Emergency Infrastructure**: Fast trigger actions linked directly to safety alerts and shared tracking channels.
- **Integrated Payments & Wallets**: Powered by Razorpay and virtual internal ledger balance transaction histories.
- **Enterprise Help Desk**: High-performance ChatGPT/Intercom style support tickets conversation interface with 5-minute message grouping intervals.

---

## 📸 Screenshots

To explore the interface layouts, check our `/docs/screenshots/` directory:
- **Landing Page**: [`/docs/screenshots/landing.png`](docs/screenshots/landing.png)
- **Booking Flow**: [`/docs/screenshots/booking.png`](docs/screenshots/booking.png)
- **Dashboard**: [`/docs/screenshots/dashboard.png`](docs/screenshots/dashboard.png)
- **Assistant KYC Onboarding**: [`/docs/screenshots/assistant_kyc.png`](docs/screenshots/assistant_kyc.png)
- **Support Inbox**: [`/docs/screenshots/support_inbox.png`](docs/screenshots/support_inbox.png)

---

## 🌟 Features

### 👤 Passenger Features
- **Secure OTP Login**: Zero-password secure authentication.
- **Ride Booking**: On-demand and scheduled ride reservations.
- **Live Tracking**: Real-time geolocation updates.
- **Internal Wallet**: In-app ledger balance deposits and transactions.
- **SOS Panic Mechanism**: Immediate safety trigger with shared trip links.

### 💼 Assistant Features
- **Multi-Document KYC Submission**: Aadhaar and police verification uploads.
- **Availability Toggle**: Real-time online/offline control.
- **Live Navigation Dashboard**: Map instructions from pickup to destination.
- **OTP Start Ride**: Secure handshakes preventing incorrect passenger pickups.

### 🔑 Admin Features
- **User & Assistant Management**: Block, suspend, or update verification statuses.
- **Operational Audits**: Complete audit trails tracking administrator actions.
- **Support Ticketing Center**: Active support ticket desk with messaging threads.

---

## 🏗️ System Architecture

```
         ┌────────────────────────┐
         │     React Frontend     │
         └───────────┬────────────┘
                     │ REST APIs & WebSockets
         ┌───────────▼────────────┐
         │    FastAPI Backend     │
         └───────────┬────────────┘
                     │ Business Services & Helpers
         ┌───────────▼────────────┐
         │   Repository Layer     │
         └───────────┬────────────┘
                     │ SQLAlchemy Async ORM
         ┌───────────▼────────────┐
         │     MySQL Database     │
         └────────────────────────┘
```

---

## 🛠️ Tech Stack

| Domain | Technologies |
| :--- | :--- |
| **Frontend** | React, TypeScript, Vite, Tailwind CSS, shadcn/ui, Framer Motion |
| **Backend** | Python, FastAPI, SQLAlchemy, Pydantic V2, Alembic |
| **Database** | MySQL (8.0), Spatial Indexes (GIS), SQLite (Development) |
| **Payments** | Razorpay SDK, virtual wallet ledgers |
| **Routing & Maps** | OpenRouteService API, Google Maps SDK, Leaflet |
| **Authentication** | JWT, Refresh Tokens, bcrypt, OTP hashes |
| **Deployment** | Docker, Docker Compose, Nginx, Gunicorn |

---

## 📁 Repository Structure

```
NavAssist/
├── backend/               # FastAPI application codebase
├── web/                   # Vite + React + TypeScript frontend codebase
├── database/              # Global schema files & migration versions
├── docs/                  # Assets, screenshots, and visual documentation
└── README.md              # Root entry landing page
```

---

## ⚙️ Installation & Setup

### 1. Clone the repository
```bash
git clone https://github.com/EswarChinthakayala-FullStack/NavAssist.git
cd NavAssist
```

### 2. Configure Environment Variables
Create a `.env` file in the `backend/` and `web/` directories with your credentials:
```ini
DATABASE_URL=mysql+aiomysql://root:password@localhost:3306/navassist
JWT_SECRET=your_jwt_secret_token_here
RAZORPAY_KEY_ID=your_razorpay_key_id
RAZORPAY_KEY_SECRET=your_razorpay_key_secret
GOOGLE_MAPS_API_KEY=your_google_maps_api_key
OPENROUTESERVICE_API_KEY=your_openrouteservice_api_key
```

### 3. Backend Setup
```bash
cd backend
python -m venv .venv
source .venv/bin/activate  # Windows: .venv\Scripts\activate
pip install -r requirements.txt
python migrate_db.py
python start.py
```

### 4. Frontend Setup
```bash
cd ../web
npm install
npm run dev
```

---

## 🔒 Security Architecture
- **JWT Session Verification**: Secure verification using short-lived tokens and cryptographic sign-offs.
- **Bcrypt Password Cryptography**: Secure hashing protecting passenger credentials.
- **SQL Injection Safeguards**: SQLAlchemy prepared statements and ORM abstractions prevent SQL injection attacks.
- **Upload Byte-Signature Check**: Upload pipelines audit and reject illegal MIME headers and scripts.

---

## 🚀 Performance Optimizations
- **Spatial Indexing**: GIS indices speed up radial proximity queries on assistant locations.
- **Lazy Loading**: Code split routes compile chunks only when accessed.
- **Database Index Optimization**: Relational databases carry targeted index keys on indexes like `booking_code`.

---

## 📄 License
This project is licensed under the MIT License - see the `LICENSE` file for details.
