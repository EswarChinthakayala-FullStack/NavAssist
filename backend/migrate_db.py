import asyncio
from sqlalchemy import text
from app.core.database import engine

async def run_migrations():
    print("Starting MySQL database schema migrations...")
    alter_queries = [
        """
        CREATE TABLE IF NOT EXISTS invoices (
            id INT AUTO_INCREMENT PRIMARY KEY,
            booking_id INT NOT NULL,
            invoice_number VARCHAR(50) NOT NULL,
            file_path VARCHAR(500) NULL,
            generated_at DATETIME NOT NULL,
            invoice_version INT NOT NULL DEFAULT 1,
            invoice_hash VARCHAR(64) NULL,
            pdf_size INT NULL,
            status VARCHAR(20) NOT NULL,
            UNIQUE KEY uq_invoice_booking (booking_id),
            UNIQUE KEY uq_invoice_number (invoice_number),
            CONSTRAINT fk_invoices_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
        ) ENGINE=InnoDB
        """,
        "ALTER TABLE bookings ADD COLUMN payment_method VARCHAR(20) DEFAULT 'online' NOT NULL",
        "ALTER TABLE bookings ADD COLUMN payment_status VARCHAR(20) DEFAULT 'pending' NOT NULL",
        "ALTER TABLE bookings ADD COLUMN payment_id INT NULL",
        "ALTER TABLE payments ADD COLUMN payment_time DATETIME NULL",
        "ALTER TABLE payments ADD COLUMN payment_reference VARCHAR(100) NULL",
        "ALTER TABLE payments ADD COLUMN receipt_number VARCHAR(100) NULL",
        "ALTER TABLE payments ADD COLUMN invoice_number VARCHAR(100) NULL",
        "ALTER TABLE payments ADD COLUMN idempotency_key VARCHAR(100) NULL",
        "UPDATE users SET created_at = NOW() WHERE created_at IS NULL OR CAST(created_at AS CHAR) LIKE '0000-00-00%'",
        "UPDATE users SET updated_at = NOW() WHERE updated_at IS NULL OR CAST(updated_at AS CHAR) LIKE '0000-00-00%'",
        "UPDATE guest_profiles SET created_at = NOW() WHERE created_at IS NULL OR CAST(created_at AS CHAR) LIKE '0000-00-00%'",
        "UPDATE assistant_profiles SET created_at = NOW() WHERE created_at IS NULL OR CAST(created_at AS CHAR) LIKE '0000-00-00%'",
        "UPDATE assistant_profiles SET updated_at = NOW() WHERE updated_at IS NULL OR CAST(updated_at AS CHAR) LIKE '0000-00-00%'",
        """
        INSERT INTO users (public_id, full_name, email, phone_number, password_hash, user_type, is_email_verified, is_phone_verified, status, created_at, updated_at)
        VALUES ('89bb86fa-ee98-4d9e-a95e-a40b6b7923f4', 'System Administrator', 'admin@navassist.in', '+919999999999', '$2b$12$1AyiyqRqnmoLXjLE/dWA7exBjG1YDaFMlxl2bkUmapxlEnzUOP/NW', 'admin', 1, 1, 'active', NOW(), NOW())
        ON DUPLICATE KEY UPDATE 
            created_at = IF(CAST(created_at AS CHAR) LIKE '0000-00-00%', NOW(), created_at),
            updated_at = IF(CAST(updated_at AS CHAR) LIKE '0000-00-00%', NOW(), updated_at)
        """,
    ]
    async with engine.begin() as conn:
        for q in alter_queries:
            try:
                await conn.execute(text(q))
                print(f"SUCCESS: {q}")
            except Exception as e:
                print(f"SKIPPED (already exists or error): {e}")

if __name__ == "__main__":
    asyncio.run(run_migrations())
