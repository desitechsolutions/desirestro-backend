-- Migration V8: Fix Bill Number Race Condition
-- This migration adds a sequence table and unique constraint to prevent duplicate bill numbers

-- Create bill sequence table for atomic sequence generation
CREATE TABLE bill_sequence (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    restaurant_id BIGINT NOT NULL,
    bill_date DATE NOT NULL,
    last_sequence INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_restaurant_date (restaurant_id, bill_date),
    FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add unique constraint on bill_number to prevent duplicates
ALTER TABLE bill ADD CONSTRAINT uk_bill_number UNIQUE (restaurant_id, bill_number);

-- Add index for better performance on bill number lookups
CREATE INDEX idx_bill_restaurant_number ON bill(restaurant_id, bill_number);

-- Create stored procedure for atomic sequence generation
DELIMITER //

CREATE PROCEDURE get_next_bill_sequence(
    IN p_restaurant_id BIGINT,
    IN p_bill_date DATE,
    OUT p_sequence INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    START TRANSACTION;
    
    -- Insert or update sequence
    INSERT INTO bill_sequence (restaurant_id, bill_date, last_sequence)
    VALUES (p_restaurant_id, p_bill_date, 1)
    ON DUPLICATE KEY UPDATE last_sequence = last_sequence + 1;
    
    -- Get the current sequence
    SELECT last_sequence INTO p_sequence
    FROM bill_sequence
    WHERE restaurant_id = p_restaurant_id AND bill_date = p_bill_date
    FOR UPDATE;
    
    COMMIT;
END //

DELIMITER ;

-- Add comment for documentation
ALTER TABLE bill_sequence COMMENT = 'Stores bill sequence numbers per restaurant per day to prevent race conditions';

-- Made with Bob
