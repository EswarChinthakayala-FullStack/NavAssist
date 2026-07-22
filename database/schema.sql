-- NavAssist Complete Database Schema (MySQL 8.0)
-- 29 Tables across 9 Domains

CREATE DATABASE IF NOT EXISTS navassist;
USE navassist;

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================
-- DOMAIN A: IDENTITY & ACCESS
-- ============================================================================

DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    public_id CHAR(36) UNIQUE NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) UNIQUE NULL,
    phone_number VARCHAR(15) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NULL,
    user_type ENUM('guest', 'assistant', 'admin') NOT NULL DEFAULT 'guest',
    profile_photo_url VARCHAR(500) NULL,
    auth_provider ENUM('local', 'google') NOT NULL DEFAULT 'local',
    google_id VARCHAR(255) UNIQUE NULL,
    is_email_verified TINYINT(1) NOT NULL DEFAULT 0,
    is_phone_verified TINYINT(1) NOT NULL DEFAULT 0,
    status ENUM('active', 'suspended', 'deleted') NOT NULL DEFAULT 'active',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS device_tokens;
CREATE TABLE device_tokens (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    fcm_token VARCHAR(500) NOT NULL,
    device_type ENUM('android', 'ios', 'web') NOT NULL,
    last_used_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_device_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    UNIQUE KEY uq_user_fcm (user_id, fcm_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS refresh_tokens;
CREATE TABLE refresh_tokens (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    device_info VARCHAR(255) NULL,
    expires_at DATETIME NOT NULL,
    revoked TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS otp_verifications;
CREATE TABLE otp_verifications (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    identifier VARCHAR(150) NOT NULL,
    otp_hash VARCHAR(255) NOT NULL,
    purpose ENUM('signup', 'login', 'reset_password') NOT NULL,
    attempt_count TINYINT NOT NULL DEFAULT 0,
    expires_at DATETIME NOT NULL,
    is_verified TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_otp_identifier (identifier),
    KEY idx_identifier_purpose (identifier, purpose)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================================================================
-- DOMAIN B: ASSISTANT & TRUST
-- ============================================================================

DROP TABLE IF EXISTS assistant_profiles;
CREATE TABLE assistant_profiles (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL UNIQUE,
    bio TEXT NULL,
    experience_years TINYINT UNSIGNED NOT NULL DEFAULT 0,
    aadhaar_number_enc VARBINARY(255) NULL,
    aadhaar_masked VARCHAR(20) NULL,
    verification_status ENUM('not_submitted', 'pending', 'verified', 'rejected') NOT NULL DEFAULT 'not_submitted',
    kyc_reviewed_by BIGINT UNSIGNED NULL,
    kyc_reviewed_at DATETIME NULL,
    trust_score DECIMAL(3,2) NOT NULL DEFAULT 0.00,
    avg_rating DECIMAL(2,1) NOT NULL DEFAULT 0.0,
    total_trips INT UNSIGNED NOT NULL DEFAULT 0,
    is_online TINYINT(1) NOT NULL DEFAULT 0,
    current_location POINT SRID 4326 NOT NULL,
    location_updated_at DATETIME NULL,
    service_radius_km DECIMAL(4,1) NOT NULL DEFAULT 10.0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    SPATIAL KEY idx_assistant_location (current_location),
    CONSTRAINT fk_assistant_profile_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_assistant_profile_admin FOREIGN KEY (kyc_reviewed_by) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS assistant_documents;
CREATE TABLE assistant_documents (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    assistant_id BIGINT UNSIGNED NOT NULL,
    doc_type ENUM('aadhaar_front', 'aadhaar_back', 'profile_photo', 'police_verification') NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    verified TINYINT(1) NOT NULL DEFAULT 0,
    uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_assistant_docs_profile FOREIGN KEY (assistant_id) REFERENCES assistant_profiles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS payout_accounts;
CREATE TABLE payout_accounts (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    assistant_id BIGINT UNSIGNED NOT NULL,
    account_holder_name VARCHAR(150) NOT NULL,
    account_number_enc VARBINARY(255) NOT NULL,
    ifsc_code VARCHAR(11) NULL,
    upi_id VARCHAR(100) NULL,
    is_verified TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payout_accounts_profile FOREIGN KEY (assistant_id) REFERENCES assistant_profiles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS payouts;
CREATE TABLE payouts (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    assistant_id BIGINT UNSIGNED NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status ENUM('pending', 'processing', 'completed', 'failed') NOT NULL DEFAULT 'pending',
    reference_id VARCHAR(100) NULL,
    initiated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at DATETIME NULL,
    CONSTRAINT fk_payouts_profile FOREIGN KEY (assistant_id) REFERENCES assistant_profiles (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================================================================
-- DOMAIN C: LOCATION & GEO
-- ============================================================================

DROP TABLE IF EXISTS saved_locations;
CREATE TABLE saved_locations (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    label ENUM('home', 'office', 'favorite', 'other') NOT NULL DEFAULT 'other',
    custom_label VARCHAR(100) NULL,
    address VARCHAR(500) NOT NULL,
    coordinates POINT SRID 4326 NOT NULL,
    place_id VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    SPATIAL KEY idx_saved_coords (coordinates),
    CONSTRAINT fk_saved_locations_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS service_points;
CREATE TABLE service_points (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    type ENUM('railway_station', 'airport', 'bus_stand') NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    code VARCHAR(20) NULL,
    coordinates POINT SRID 4326 NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    SPATIAL KEY idx_service_point_coords (coordinates)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================================================================
-- DOMAIN D: BOOKING & MATCHING
-- ============================================================================

DROP TABLE IF EXISTS coupons;
CREATE TABLE coupons (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) UNIQUE NOT NULL,
    description VARCHAR(255) NOT NULL,
    discount_type ENUM('flat', 'percentage') NOT NULL,
    discount_value DECIMAL(8,2) NOT NULL,
    max_discount_amount DECIMAL(8,2) NULL,
    min_booking_amount DECIMAL(8,2) NOT NULL DEFAULT 0.00,
    usage_limit_per_user SMALLINT NULL,
    total_usage_limit INT NULL,
    valid_from DATETIME NOT NULL,
    valid_to DATETIME NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS bookings;
CREATE TABLE bookings (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    booking_code VARCHAR(20) UNIQUE NOT NULL,
    guest_id BIGINT UNSIGNED NOT NULL,
    assistant_id BIGINT UNSIGNED NULL,
    pickup_service_point_id BIGINT UNSIGNED NULL,
    pickup_address VARCHAR(500) NOT NULL,
    pickup_coordinates POINT SRID 4326 NOT NULL,
    destination_address VARCHAR(500) NOT NULL,
    destination_coordinates POINT SRID 4326 NOT NULL,
    scheduled_at DATETIME NULL,
    status ENUM('pending', 'searching', 'assigned', 'assistant_enroute', 'arrived_pickup', 'guest_picked_up', 'in_progress', 'completed', 'cancelled', 'no_show') NOT NULL DEFAULT 'pending',
    distance_km DECIMAL(6,2) NULL,
    estimated_duration_min SMALLINT NULL,
    fare_estimate DECIMAL(10,2) NULL,
    final_fare DECIMAL(10,2) NULL,
    coupon_id BIGINT UNSIGNED NULL,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    payment_method VARCHAR(20) DEFAULT 'online' NOT NULL,
    payment_status VARCHAR(20) DEFAULT 'pending' NOT NULL,
    payment_id BIGINT UNSIGNED NULL,
    cancellation_reason VARCHAR(255) NULL,
    cancelled_by ENUM('guest', 'assistant', 'admin', 'system') NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    SPATIAL KEY idx_booking_pickup_location (pickup_coordinates),
    SPATIAL KEY idx_booking_dest_location (destination_coordinates),
    KEY idx_booking_guest_status (guest_id, status),
    KEY idx_booking_assistant_status (assistant_id, status),
    CONSTRAINT fk_bookings_guest FOREIGN KEY (guest_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_bookings_assistant FOREIGN KEY (assistant_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_bookings_point FOREIGN KEY (pickup_service_point_id) REFERENCES service_points (id) ON DELETE RESTRICT,
    CONSTRAINT fk_bookings_coupon FOREIGN KEY (coupon_id) REFERENCES coupons (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS booking_status_history;
CREATE TABLE booking_status_history (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT UNSIGNED NOT NULL,
    status VARCHAR(30) NOT NULL,
    changed_by BIGINT UNSIGNED NULL,
    changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_history_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_history_user FOREIGN KEY (changed_by) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS live_locations;
CREATE TABLE live_locations (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT UNSIGNED NOT NULL,
    actor_type ENUM('guest', 'assistant') NOT NULL,
    coordinates POINT SRID 4326 NOT NULL,
    heading DECIMAL(5,2) NULL,
    recorded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    SPATIAL KEY idx_live_coordinates (coordinates),
    KEY idx_live_booking (booking_id, recorded_at),
    CONSTRAINT fk_live_locations_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS coupon_redemptions;
CREATE TABLE coupon_redemptions (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    coupon_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    booking_id BIGINT UNSIGNED NOT NULL UNIQUE,
    redeemed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_redemptions_coupon FOREIGN KEY (coupon_id) REFERENCES coupons (id) ON DELETE RESTRICT,
    CONSTRAINT fk_redemptions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_redemptions_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS booking_messages;
CREATE TABLE booking_messages (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT UNSIGNED NOT NULL,
    sender_id BIGINT UNSIGNED NOT NULL,
    message_type ENUM('text', 'image', 'location') NOT NULL DEFAULT 'text',
    content VARCHAR(2000) NULL,
    media_url VARCHAR(500) NULL,
    latitude VARCHAR(30) NULL,
    longitude VARCHAR(30) NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_msg_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_msg_sender FOREIGN KEY (sender_id) REFERENCES users (id) ON DELETE CASCADE,
    KEY idx_booking_msg_booking (booking_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================================================================
-- DOMAIN E: PAYMENTS & WALLET
-- ============================================================================

DROP TABLE IF EXISTS payments;
CREATE TABLE payments (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'INR',
    payment_method ENUM('upi', 'card', 'netbanking', 'wallet') NULL,
    gateway VARCHAR(30) NOT NULL DEFAULT 'razorpay',
    gateway_order_id VARCHAR(100) UNIQUE NULL,
    gateway_payment_id VARCHAR(100) UNIQUE NULL,
    gateway_signature VARCHAR(255) NULL,
    status ENUM('created', 'authorized', 'captured', 'failed', 'refunded') NOT NULL DEFAULT 'created',
    payment_time DATETIME NULL,
    payment_reference VARCHAR(100) UNIQUE NULL,
    receipt_number VARCHAR(100) NULL,
    invoice_number VARCHAR(100) NULL,
    idempotency_key VARCHAR(100) UNIQUE NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE,
    CONSTRAINT fk_payments_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS wallets;
CREATE TABLE wallets (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL UNIQUE,
    balance DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    currency CHAR(3) NOT NULL DEFAULT 'INR',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_wallets_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS wallet_transactions;
CREATE TABLE wallet_transactions (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    wallet_id BIGINT UNSIGNED NOT NULL,
    type ENUM('credit', 'debit') NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    reference_type ENUM('booking', 'refund', 'topup', 'payout') NOT NULL,
    reference_id BIGINT UNSIGNED NULL,
    balance_after DECIMAL(10,2) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wallet_tx_wallet FOREIGN KEY (wallet_id) REFERENCES wallets (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS invoices;
CREATE TABLE invoices (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT UNSIGNED NOT NULL UNIQUE,
    invoice_number VARCHAR(50) UNIQUE NOT NULL,
    file_path VARCHAR(500) NULL,
    generated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    invoice_version INT NOT NULL DEFAULT 1,
    invoice_hash VARCHAR(64) NULL,
    pdf_size INT NULL,
    status ENUM('generating', 'generated', 'failed') NOT NULL DEFAULT 'generating',
    CONSTRAINT fk_invoices_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE,
    KEY idx_invoice_num (invoice_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================================================================
-- DOMAIN F: SAFETY
-- ============================================================================

DROP TABLE IF EXISTS sos_alerts;
CREATE TABLE sos_alerts (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT UNSIGNED NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    coordinates POINT SRID 4326 NOT NULL,
    status ENUM('active', 'resolved', 'false_alarm') NOT NULL DEFAULT 'active',
    triggered_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at DATETIME NULL,
    resolved_by BIGINT UNSIGNED NULL,
    SPATIAL KEY idx_sos_coordinates (coordinates),
    CONSTRAINT fk_sos_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE,
    CONSTRAINT fk_sos_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_sos_admin FOREIGN KEY (resolved_by) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS emergency_contacts;
CREATE TABLE emergency_contacts (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(150) NOT NULL,
    phone_number VARCHAR(15) NOT NULL,
    relationship VARCHAR(50) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_emergency_contacts_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS trip_shares;
CREATE TABLE trip_shares (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT UNSIGNED NOT NULL,
    share_token CHAR(32) UNIQUE NOT NULL,
    created_by BIGINT UNSIGNED NOT NULL,
    expires_at DATETIME NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trip_shares_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE,
    CONSTRAINT fk_trip_shares_user FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================================================================
-- DOMAIN G: ENGAGEMENT
-- ============================================================================

DROP TABLE IF EXISTS ratings_reviews;
CREATE TABLE ratings_reviews (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT UNSIGNED NOT NULL UNIQUE,
    rated_by BIGINT UNSIGNED NOT NULL,
    rated_assistant_id BIGINT UNSIGNED NOT NULL,
    rating TINYINT UNSIGNED NOT NULL,
    review_text VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ratings_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE,
    CONSTRAINT fk_ratings_guest FOREIGN KEY (rated_by) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_ratings_assistant FOREIGN KEY (rated_assistant_id) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS notifications;
CREATE TABLE notifications (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    title VARCHAR(150) NOT NULL,
    body VARCHAR(500) NOT NULL,
    type ENUM('booking', 'payment', 'promo', 'system', 'safety') NOT NULL,
    data JSON NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS app_settings;
CREATE TABLE app_settings (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL UNIQUE,
    language VARCHAR(10) NOT NULL DEFAULT 'en',
    dark_mode TINYINT(1) NOT NULL DEFAULT 0,
    notifications_enabled TINYINT(1) NOT NULL DEFAULT 1,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_settings_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================================================================
-- DOMAIN H: SUPPORT & ADMIN
-- ============================================================================

DROP TABLE IF EXISTS support_tickets;
CREATE TABLE support_tickets (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    subject VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    status ENUM('open', 'in_progress', 'resolved', 'closed') NOT NULL DEFAULT 'open',
    priority ENUM('low', 'medium', 'high') NOT NULL DEFAULT 'medium',
    assigned_to BIGINT UNSIGNED NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_support_tickets_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_support_tickets_admin FOREIGN KEY (assigned_to) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS support_ticket_messages;
CREATE TABLE support_ticket_messages (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT UNSIGNED NOT NULL,
    sender_id BIGINT UNSIGNED NOT NULL,
    message TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ticket_msg_ticket FOREIGN KEY (ticket_id) REFERENCES support_tickets (id) ON DELETE CASCADE,
    CONSTRAINT fk_ticket_msg_sender FOREIGN KEY (sender_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS faqs;
CREATE TABLE faqs (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    category VARCHAR(50) NOT NULL,
    question VARCHAR(255) NOT NULL,
    answer TEXT NOT NULL,
    display_order SMALLINT NOT NULL DEFAULT 0,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS audit_logs;
CREATE TABLE audit_logs (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    admin_id BIGINT UNSIGNED NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NULL,
    entity_id BIGINT UNSIGNED NULL,
    metadata JSON NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_logs_admin FOREIGN KEY (admin_id) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS booking_reports;
CREATE TABLE booking_reports (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    report_number VARCHAR(50) UNIQUE NOT NULL,
    booking_id BIGINT UNSIGNED NOT NULL,
    reporter_id BIGINT UNSIGNED NOT NULL,
    against_user_id BIGINT UNSIGNED NULL,
    category ENUM('passenger_safety', 'assistant_behavior', 'late_arrival', 'wrong_route', 'fare_issue', 'payment_issue', 'harassment', 'emergency', 'vehicle_issue', 'lost_item', 'technical_problem', 'other') NOT NULL,
    severity ENUM('low', 'medium', 'high', 'critical') NOT NULL DEFAULT 'medium',
    status ENUM('submitted', 'under_review', 'waiting_response', 'resolved', 'closed', 'rejected') NOT NULL DEFAULT 'submitted',
    description VARCHAR(1000) NOT NULL,
    evidence JSON NULL,
    assigned_admin_id BIGINT UNSIGNED NULL,
    resolution_notes VARCHAR(1000) NULL,
    resolution_time DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_report_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_report_reporter FOREIGN KEY (reporter_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_report_against FOREIGN KEY (against_user_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_booking_report_admin FOREIGN KEY (assigned_admin_id) REFERENCES users (id) ON DELETE SET NULL,
    KEY idx_booking_report_num (report_number),
    KEY idx_booking_report_booking (booking_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================================================================
-- DOMAIN I: PRICING & PROMOTIONS
-- ============================================================================

DROP TABLE IF EXISTS fare_rules;
CREATE TABLE fare_rules (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    service_point_type ENUM('railway_station', 'airport', 'bus_stand', 'general') NOT NULL,
    base_fare DECIMAL(8,2) NOT NULL,
    per_km_rate DECIMAL(6,2) NOT NULL,
    per_min_rate DECIMAL(6,2) NOT NULL,
    min_fare DECIMAL(8,2) NOT NULL,
    surge_multiplier DECIMAL(3,2) NOT NULL DEFAULT 1.00,
    effective_from DATETIME NOT NULL,
    effective_to DATETIME NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


SET FOREIGN_KEY_CHECKS = 1;


-- ============================================================================
-- DATABASE TRIGGERS
-- ============================================================================

DELIMITER //

-- 1. Trigger for logging status history changes on bookings automatically
DROP TRIGGER IF EXISTS trg_booking_status_history//
CREATE TRIGGER trg_booking_status_history
AFTER UPDATE ON bookings
FOR EACH ROW
BEGIN
    IF NEW.status <> OLD.status THEN
        INSERT INTO booking_status_history (booking_id, status, changed_by, changed_at)
        VALUES (
            NEW.id, 
            NEW.status, 
            COALESCE(NEW.assistant_id, NEW.guest_id), 
            NOW()
        );
    END IF;
END//

-- 2. Trigger for automated ratings update on assistant profiles upon new ratings insertion
DROP TRIGGER IF EXISTS trg_ratings_reviews_insert//
CREATE TRIGGER trg_ratings_reviews_insert
AFTER INSERT ON ratings_reviews
FOR EACH ROW
BEGIN
    UPDATE assistant_profiles 
    SET 
        total_trips = total_trips + 1,
        avg_rating = (
            SELECT ROUND(AVG(rating), 1) 
            FROM ratings_reviews 
            WHERE rated_assistant_id = NEW.rated_assistant_id
        )
    WHERE user_id = NEW.rated_assistant_id;
END//

-- 3. Trigger for ledger balance management on wallet insertions
DROP TRIGGER IF EXISTS trg_wallet_transaction_insert//
CREATE TRIGGER trg_wallet_transaction_insert
AFTER INSERT ON wallet_transactions
FOR EACH ROW
BEGIN
    IF NEW.type = 'credit' THEN
        UPDATE wallets 
        SET balance = balance + NEW.amount 
        WHERE id = NEW.wallet_id;
    ELSE
        UPDATE wallets 
        SET balance = balance - NEW.amount 
        WHERE id = NEW.wallet_id;
    END IF;
END//

DELIMITER ;


-- ============================================================================
-- SEED DATA & BOOTSTRAP
-- ============================================================================

-- Seed default System Admin account
-- Password: Password123
INSERT INTO users (public_id, full_name, email, phone_number, password_hash, user_type, is_email_verified, is_phone_verified, status, created_at, updated_at)
VALUES (
    '89bb86fa-ee98-4d9e-a95e-a40b6b7923f4',
    'System Administrator',
    'admin@navassist.in',
    '+919999999999',
    '$2b$12$1AyiyqRqnmoLXjLE/dWA7exBjG1YDaFMlxl2bkUmapxlEnzUOP/NW',
    'admin',
    1,
    1,
    'active',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON DUPLICATE KEY UPDATE 
    created_at = IF(CAST(created_at AS CHAR) LIKE '0000-00-00%', CURRENT_TIMESTAMP, created_at),
    updated_at = IF(CAST(updated_at AS CHAR) LIKE '0000-00-00%', CURRENT_TIMESTAMP, updated_at);

