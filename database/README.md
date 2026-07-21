<p align="center">
  <img src="../docs/assets/logo.svg" alt="NavAssist Logo" width="100">
</p>

<h1 align="center">NavAssist Database Schema</h1>

<p align="center">
  Relational Database Schema & GIS Telemetry Config
</p>

---

## 🛠️ Overview

NavAssist is backed by a structured relational database layout containing **29 tables** partitioned across **9 operational domains**. It is configured to run on **MySQL 8.0** (via local setups such as XAMPP) or standard cloud database engines.

---

## 🗺️ Schema Domains

| Domain | Scope | Primary Tables |
| :--- | :--- | :--- |
| **Domain A (Identity)** | Users, auth credentials, tokens | `users`, `device_tokens`, `refresh_tokens`, `otp_verifications` |
| **Domain B (Trust & KYC)** | Assistant profiles, documents, payouts | `assistant_profiles`, `assistant_documents`, `payout_accounts` |
| **Domain C (Location & Geo)** | Saved points and radial configurations | `saved_locations`, `service_points` |
| **Domain D (Bookings)** | Core bookings, history, and message streams | `bookings`, `booking_status_history`, `booking_messages` |
| **Domain E (Payments)** | Razorpay order tracking and virtual wallets | `payments`, `wallets`, `wallet_transactions`, `invoices` |
| **Domain F (Safety)** | Emergency contacts and SOS tracking | `sos_alerts`, `emergency_contacts`, `trip_shares` |
| **Domain G (Engagement)** | Ratings, notifications, user settings | `ratings_reviews`, `notifications`, `app_settings` |
| **Domain H (Support)** | Operator helpdesk ticketing and action logs | `support_tickets`, `support_ticket_messages`, `audit_logs` |
| **Domain I (Pricing)** | Base fares, per-km rates, and surge metrics | `fare_rules` |



## ⚡ Database Features

### 1. Spatial Telemetry (GIS)
- Utilizes `POINT SRID 4326` datatypes for storing coordinates on `assistant_profiles` and `live_locations`.
- Optimized via `SPATIAL KEY` indexes for fast geo-radial lookups.

### 2. Triggers
The schema implements automated data sync rules:
- **`trg_booking_status_history`**: Inserts a new timeline log whenever a booking's status changes.
- **`trg_ratings_reviews_insert`**: Calculates and updates average stars and trip totals on the assistant's profile when a review is submitted.
- **`trg_wallet_transaction_insert`**: Modifies the parent wallet balance when a debit/credit transaction occurs.

---

## 📥 Import and Setup (XAMPP / Local MySQL)

1. Start **MySQL** via the XAMPP Control Panel.
2. Open phpMyAdmin or your MySQL CLI:
   ```bash
   mysql -u root -p
   ```
3. Create the database:
   ```sql
   CREATE DATABASE navassist;
   ```
4. Import the schema file:
   ```bash
   mysql -u root -p navassist < database/schema.sql
   ```

---

## 💾 Backups & Versioning
- **Backup**:
  ```bash
  mysqldump -u root -p navassist > database/schema_backup.sql
  ```
- **Migrations**: Alembic handles python-model changes and maps them to migrations inside `backend/database/versions/`.
