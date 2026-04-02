-- =====================================================
-- V8: Atomic bill-number sequence table
-- The UNIQUE KEY uk_bill_number is already defined on
-- the bill table created in V7; no need to add it here.
-- =====================================================

CREATE TABLE IF NOT EXISTS bill_sequence (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    restaurant_id  BIGINT NOT NULL,
    bill_date      DATE NOT NULL,
    last_sequence  INT NOT NULL DEFAULT 0,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_bill_seq_restaurant_date (restaurant_id, bill_date),
    FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- NOTE: A stored procedure for atomic sequence generation was intentionally omitted.
-- Flyway does not support MySQL DELIMITER directives. Sequence logic is handled
-- in BillingService.generateBillNumber() using database row locking (SELECT FOR UPDATE).

ALTER TABLE bill_sequence
    COMMENT = 'Stores per-restaurant per-day bill sequence numbers to prevent race conditions';
