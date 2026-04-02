-- V5: Add email to users + password reset tokens table
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- HELPER PROCEDURE: AddColumnUnlessExists
-- =====================================================
DROP PROCEDURE IF EXISTS AddColumnUnlessExists;
DELIMITER //

CREATE PROCEDURE AddColumnUnlessExists(
    IN tableName VARCHAR(64),
    IN columnName VARCHAR(64),
    IN columnDefinition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = tableName
        AND COLUMN_NAME = columnName
    ) THEN
        SET @sql = CONCAT('ALTER TABLE ', tableName, ' ADD COLUMN ', columnName, ' ', columnDefinition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //

DELIMITER ;

-- =====================================================
-- USERS: Add email column
-- =====================================================
CALL AddColumnUnlessExists('users', 'email', 'VARCHAR(150) NULL');

-- =====================================================
-- PASSWORD RESET TOKENS
-- =====================================================
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    token         VARCHAR(255) NOT NULL,
    user_id       BIGINT NOT NULL,
    restaurant_id BIGINT NOT NULL,
    expiry_date   DATETIME NOT NULL,
    used          BOOLEAN NOT NULL DEFAULT FALSE,
    used_at       DATETIME NULL,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_prt_token (token),
    INDEX idx_prt_user (user_id),
    CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_prt_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- CLEANUP
-- =====================================================
DROP PROCEDURE IF EXISTS AddColumnUnlessExists;

SET FOREIGN_KEY_CHECKS = 1;