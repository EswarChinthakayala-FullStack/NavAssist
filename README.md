# NavAssist - Full-Stack Ride Assistance Platform

NavAssist is an executive-level, full-stack customer support and ride assistance management platform. It features an automated KYC onboarding portal, a real-time ride and assistant booking dashboard, and an enterprise-grade customer support thread system.

---

## 🚀 Key Features

### 1. Enterprise Support Conversation UI
- **ChatGPT-Style Chat Stream**: Features message grouping that compiles consecutive replies sent by the same user within 5-minute windows under a single header line.
- **Shrink-Wrap Messages**: Chat bubbles fit their content width cleanly with zero wasted horizontal or vertical white space.
- **Stand-alone Image Cards**: Attachment messages containing only image files render as clean, borderless standalone cards with a smooth lightbox zoom/pan viewer.
- **Responsive 3-Column Layout**:
  - **Main Thread**: Timeline with date separators ("Today", "Yesterday") and auto-scrolling container.
  - **Sticky Audit Sidebar**: Live segmented ticket status pills, customer profiles, and a GitHub-style vertical audit trail.
  - **Mobile sheet drawer** for ticket attributes.

### 2. KYC Verification & Ride Booking Portal
- **Verification Flow**: Secure identity validation, document uploads (KYC front/back), and state validation on user profile statuses.
- **Booking Engine**: Live route calculations, price estimation models, assistant matching, and Razorpay client integrations.

### 3. Production-Ready API & Upload Pipeline
- **Zero-Latency Attachment Preview**: The frontend renders local blob URLs instantly before the server upload completes to ensure smooth visual transitions.
- **Multi-Part Upload Header Interceptor**: Fixes Axios 1.x Header removal behaviors to correctly inject boundary limits on file streams.
- **Strict Byte-Signature Verification**: Prevents invalid HTML and corrupted script payloads from being uploaded to the backend uploads library.

---

## 🛠️ Technology Stack

- **Backend Architecture**: FastAPI, SQLAlchemy (Async), Alembic, Pydantic V2, pytest.
- **Frontend Architecture**: React, TypeScript, Vite, Tailwind CSS, shadcn/ui, HugeIcons, Framer Motion.
- **Storage Layer**: SQLite database engine, local filesystem uploads.

---

## 📁 Repository Structure

```
NavAssist/
├── backend/               # FastAPI python backend application
│   ├── app/               # Application source code
│   │   ├── api/           # API routes & schemas
│   │   ├── models/        # SQLAlchemy ORM models
│   │   ├── services/      # Business logic controllers
│   │   └── uploads/       # Directory for user-uploaded documents (gitignored)
│   ├── database/          # Local database files & Alembic configuration
│   ├── tests/             # Pytest automated test suites
│   └── requirements.txt   # Python package dependencies
├── web/                   # Vite + React + TypeScript frontend
│   ├── src/
│   │   ├── components/    # Reusable shadcn/ui and custom components
│   │   ├── pages/         # Page layout modules (support, booking, trip)
│   │   ├── services/      # API communication clients (Axios)
│   │   └── store/         # Zustand and context state managers
│   └── package.json       # Node package dependencies
├── database/              # Global schema specifications
└── .gitignore             # Project-wide ignore rules
```

---

## ⚙️ Setup and Installation

### Prerequisites
- Python 3.10+
- Node.js 18+

### 1. Backend Setup
1. Navigate to the backend directory:
   ```bash
   cd backend
   ```
2. Create and activate a Python virtual environment:
   ```bash
   python -m venv .venv
   # Windows:
   .venv\Scripts\activate
   # Linux/macOS:
   source .venv/bin/activate
   ```
3. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```
4. Configure environment variables (create a `.env` file):
   ```bash
   cp .env.example .env
   ```
5. Apply database migrations:
   ```bash
   python migrate_db.py
   ```
6. Run the backend server:
   ```bash
   python start.py
   ```
   The API will be available at `http://127.0.0.1:8000`.

### 2. Frontend Setup
1. Navigate to the frontend directory:
   ```bash
   cd web
   ```
2. Install npm packages:
   ```bash
   npm install
   ```
3. Run the development server:
   ```bash
   npm run dev
   ```
   Open `http://localhost:5173` in your browser.

---

## 🧪 Testing and Verification

- **Backend tests**:
  ```bash
  cd backend
  python -m pytest
  ```
- **Frontend build compilation**:
  ```bash
  cd web
  npm run build
  ```
