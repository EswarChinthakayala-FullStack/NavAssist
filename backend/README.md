<p align="center">
  <img src="../docs/assets/logo.svg" alt="NavAssist Logo" width="100">
</p>

<h1 align="center">NavAssist Backend</h1>

<p align="center">
  FastAPI Engine & Business Services Pipeline
</p>

---

## 🛠️ Overview

The NavAssist backend is a high-performance Python application built on top of **FastAPI**. It handles REST API request-response pipelines, real-time WebSockets, secure authentication, and spatial calculations.

### Tech Stack:
- **Core Framework**: FastAPI (Asynchronous)
- **Database ORM**: SQLAlchemy (with AsyncIO support)
- **Data Validation**: Pydantic V2
- **Database Migrations**: Alembic
- **Task Workers**: Python background threads and async loops
- **Cache & Telemetry**: Redis cache (optional/dev configurations)

---

## 🏗️ Architecture Layering

The codebase enforces strict separation of concerns using clean architecture patterns:

```
    ┌───────────────────────────┐
    │       FastAPI Router      │  <-- Routing & Schemas Verification
    └─────────────┬─────────────┘
                  │
    ┌─────────────▼─────────────┐
    │      Service Modules      │  <-- Business Logic & Integrations
    └─────────────┬─────────────┘
                  │
    ┌─────────────▼─────────────┐
    │     Repository Pattern    │  <-- Raw Database Queries & Mutations
    └─────────────┬─────────────┘
                  │
    ┌─────────────▼─────────────┐
    │      MySQL Database       │  <-- Spatial (GIS) Storage & Triggers
    └───────────────────────────┘
```

---

## 📁 Codebase Layout

```
backend/
├── app/
│   ├── api/             # API routes & endpoint definitions (v1)
│   ├── core/            # JWT config, database connection, and security
│   ├── models/          # SQLAlchemy Database ORM tables
│   ├── schemas/         # Pydantic validation schemas
│   ├── repositories/    # Database query wrappers
│   ├── services/        # Business logic controllers
│   ├── middlewares/     # Logging and request correlation tracking
│   └── templates/       # HTML components for email & invoice generation
├── database/            # Alembic config and schema files
└── tests/               # Pytest integration tests
```

---

## 🔑 Authentication & Session Flow

```
   ┌───────────┐      ┌───────────┐      ┌───────────┐      ┌───────────┐
   │ User Logs │ ---> │    OTP    │ ---> │  Access   │ ---> │  Refresh  │
   │ In Phone  │      │ Generated │      │ JWT (1hr) │      │ Token(7d) │
   └───────────┘      └───────────┘      └───────────┘      └───────────┘
```

---

## 📊 Database Relationships & Domains

The database schema is divided into distinct operational boundaries:
- **Identity (Domain A)**: `users`, `device_tokens`, `refresh_tokens`, `otp_verifications`.
- **Assistant & KYC (Domain B)**: `assistant_profiles`, `assistant_documents`, `payout_accounts`.
- **Booking Core (Domain D)**: `bookings`, `booking_status_history`, `booking_messages`.
- **Payments & Wallets (Domain E)**: `payments`, `wallets`, `wallet_transactions`, `invoices`.
- **Audit & Tickets (Domain H)**: `support_tickets`, `support_ticket_messages`, `audit_logs`.

---

## 🚀 Running the API Locally

### 1. Setup Virtual Environment
```bash
python -m venv .venv
# Windows:
.venv\Scripts\activate
# Linux/macOS:
source .venv/bin/activate
```

### 2. Install Packages
```bash
pip install -r requirements.txt
```

### 3. Setup database migrations and run
```bash
python migrate_db.py
python start.py
```

---

## 📋 API Documentation

FastAPI automatically generates interactive Swagger and ReDoc documentation:
- **Swagger UI**: `http://localhost:8000/docs`
- **ReDoc UI**: `http://localhost:8000/redoc`

---

## ⚙️ Environment Variables

| Variable | Description |
| :--- | :--- |
| `DATABASE_URL` | MySQL Connection String |
| `SECRET_KEY` | JWT Signatures Token Key |
| `REDIS_HOST` | Redis Server Host |
| `RAZORPAY_KEY_ID` | Razorpay Key ID |
| `RAZORPAY_KEY_SECRET` | Razorpay Secret |

---

## 💻 Logging & Monitoring
The application utilizes structured JSON logs containing:
- **Correlation ID**: Tracks request pathways across routers and middleware.
- **Process Timer**: Measures exact response generation speeds.
