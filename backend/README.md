# NavAssist Backend Service

High-performance, scalable, real-time FastAPI backend powering the NavAssist Guest and Assistant ecosystem. This service handles authentication, match-making, real-time geolocation tracking via Redis-backed WebSockets, KYC verification, safety shares, and payment processing.

---

## 🛠️ Architecture & Technology Stack

* **API Core:** FastAPI (Python 3.13, asynchronous ASGI framework)
* **Real-time Engine:** WebSockets integrated with Redis Pub/Sub for sub-100ms GPS telemetry broadcasting
* **Primary Database:** MySQL 8.0 with `SPATIAL` spatial data extensions for geolocation indexing
* **Cache & Key-Value Store:** Redis 7 (stores active assistant locations, WebSocket session states, and rate limits)
* **Task Queue:** Celery 5.6 for asynchronous background jobs (OTPs, notifications, payment lifecycle actions)
* **ORM & Migrations:** SQLAlchemy 2.0 (Async Engine) + Alembic
* **Security:** JWT Authentication tokens with direct `bcrypt` hashing configurations

---

## 🌟 Key Features

1. **Open-Source Mapping & Spatial Routing:**
   * **Route calculations & ETA matrix:** Handled via OSRM (Open Source Routing Machine) API.
   * **Geocoding & Address Resolution:** Handled via OpenStreetMap Nominatim and Photon APIs.
   * *Zero third-party API keys required out of the box.*
2. **Real-time Live Location Tracking:**
   * WebSockets endpoint `/ws/tracking/{booking_id}` facilitates live GPS updates.
   * Real-time heading, speed, milestone tracking, and dynamic ETA updates.
3. **KYC Verification & Local Storage:**
   * Purely local document storage in the [backend/uploads/](file:///e:/NavAssist/backend/uploads) directory.
   * No AWS S3 dependencies required.
4. **Third-Party Integrations:**
   * **SMS Verification:** Real OTP dispatches powered by Twilio.
   * **Payments:** In-app booking payment lifecycle powered by Razorpay checkout and webhooks.

---

## 🚀 Live Demo Cockpit Simulator

The application mounts a premium, interactive simulation cockpit served at `/web` to let you test real-time tracking, SOS panics, and booking state transitions instantly:

* **FastAPI Swagger Docs:** [http://localhost:8000/docs](http://localhost:8000/docs)
* **Diagnostics Health Check:** [http://localhost:8000/health](http://localhost:8000/health)

---

## 💻 Running Locally

### Option A: Running with Docker (Recommended)

To run the entire service stack (FastAPI, MySQL, Redis, Celery Worker) inside containers:

```bash
# Start all services collectively
docker-compose up --build
```

*Note: The standalone [Dockerfile](file:///e:/NavAssist/backend/Dockerfile) is configured to run `python start.py` by default, launching both FastAPI and the Celery background worker inside the same container environment when deployed standalone.*

---

### Option B: Bare-Metal Setup (Local Development)

#### 1. Setup Environment
Copy the example environment file and fill in your variables (such as Twilio and Razorpay credentials):
```bash
cp .env.example .env
```

#### 2. Initialize Virtual Environment & Install Dependencies
```bash
# Create the virtual environment
python -m venv .venv

# Activate the virtual environment
# On Windows:
.venv\Scripts\activate
# On macOS/Linux:
source .venv/bin/activate

# Install requirements
pip install -r requirements.txt
```

#### 3. Setup Database & Run Migrations
Ensure you have MySQL running locally and database `navassist` created, then run:
```bash
# Run migrations using Alembic
alembic upgrade head
```

#### 4. Run Both FastAPI Server & Celery Worker
Instead of starting them manually in separate windows, use the professional `start.py` script which handles orchestration, OS-specific Celery options (e.g. Windows solo pools), and graceful Ctrl+C process shutdown:
```bash
# Run both FastAPI and Celery worker concurrently
python start.py --reload
```

*Note: You can configure the host and port via CLI flags (e.g., `python start.py --host 0.0.0.0 --port 8000`). Run `python start.py --help` to view all option flags.*

#### 5. Running Manually (Alternative)
If you prefer to run services in separate terminal windows:
* **FastAPI Server:**
  ```bash
  uvicorn app.main:app --reload
  ```
* **Celery Worker:**
  ```bash
  celery -A app.core.celery_app worker --loglevel=info
  ```

---

## 🧪 Testing

The backend includes a comprehensive, asynchronous test suite verifying auth, payments, KYC verification, and tracking workflows.

```bash
# Run the test suite under the virtual environment
.venv\Scripts\python -m pytest
```

---

## 📁 Repository Layout

```
backend/
├── app/
│   ├── api/             # Versioned REST endpoints & WebSocket routers
│   ├── core/            # Configuration loaders, security, database & celery clients
│   ├── models/          # SQLAlchemy SQL models
│   ├── repositories/    # Database transaction repositories (CRUD)
│   ├── schemas/         # Pydantic schemas (request/response models)
│   ├── services/        # Business logic layers (Auth, KYC, Booking, Geo)
│   └── integrations/    # Third-party wraps (Twilio, Razorpay, OSM, Local Storage)
├── database/            # Alembic migration configurations & SQL files
├── tests/               # Pytest suite
├── uploads/             # Local KYC document uploads folder
└── .venv/               # Virtual environment folder
```
