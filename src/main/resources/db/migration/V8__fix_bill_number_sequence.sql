-- V8: Atomic bill-number sequence table + procedure
-- MySQL 8+ compatible.
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- BILL_SEQUENCE: Per-restaurant per-day counters
-- =====================================================
CREATE TABLE IF NOT EXISTS bill_sequence (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    restaurant_id  BIGINT NOT NULL,
    bill_date      DATE NOT NULL,
    last_sequence  INT NOT NULL DEFAULT 0,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_bill_seq_restaurant_date UNIQUE (restaurant_id, bill_date),
    CONSTRAINT fk_bill_seq_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT = 'Prevents race conditions in bill numbering';

-- =====================================================
-- ATOMIC SEQUENCE PROCEDURE
-- =====================================================
-- Using standard Flyway delimiter approach
DROP PROCEDURE IF EXISTS get_next_bill_sequence;

DELIMITER $$

CREATE PROCEDURE get_next_bill_sequence(
    IN  p_restaurant_id BIGINT,
    IN  p_bill_date     DATE,
    OUT p_sequence      INT
)
BEGIN
    -- Error handling to rollback on failure
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    -- Insert new row for the day or increment existing counter
    INSERT INTO bill_sequence (restaurant_id, bill_date, last_sequence)
    VALUES (p_restaurant_id, p_bill_date, 1)
    ON DUPLICATE KEY UPDATE
        last_sequence = last_sequence + 1,
        updated_at = CURRENT_TIMESTAMP;

    -- Lock the row and retrieve the incremented value
    SELECT last_sequence INTO p_sequence
    FROM   bill_sequence
    WHERE  restaurant_id = p_restaurant_id
      AND  bill_date     = p_bill_date
    FOR UPDATE;

    COMMIT;
END$$

DELIMITER ;

SET FOREIGN_KEY_CHECKS = 1;