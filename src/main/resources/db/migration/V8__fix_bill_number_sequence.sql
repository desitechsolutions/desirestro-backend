-- =====================================================
-- V8: Atomic bill-number sequence table + procedure
-- MySQL 8+ compatible.
-- Uses Flyway per-file delimiter: END$$ terminates the
-- CREATE PROCEDURE statement; all other DML uses $$ too.
-- =====================================================
-- @delimiter $$

CREATE TABLE IF NOT EXISTS bill_sequence (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    restaurant_id  BIGINT NOT NULL,
    bill_date      DATE NOT NULL,
    last_sequence  INT NOT NULL DEFAULT 0,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_bill_seq_restaurant_date (restaurant_id, bill_date),
    FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT = 'Stores per-restaurant per-day bill sequence numbers to prevent race conditions'$$

-- Atomic sequence generation: inserts a new row or increments the counter,
-- then returns the new sequence value using a SELECT FOR UPDATE lock to
-- guarantee uniqueness even under concurrent requests.
CREATE PROCEDURE IF NOT EXISTS get_next_bill_sequence(
    IN  p_restaurant_id BIGINT,
    IN  p_bill_date     DATE,
    OUT p_sequence      INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    INSERT INTO bill_sequence (restaurant_id, bill_date, last_sequence)
    VALUES (p_restaurant_id, p_bill_date, 1)
    ON DUPLICATE KEY UPDATE last_sequence = last_sequence + 1;

    SELECT last_sequence INTO p_sequence
    FROM   bill_sequence
    WHERE  restaurant_id = p_restaurant_id
      AND  bill_date     = p_bill_date
    FOR UPDATE;

    COMMIT;
END$$
