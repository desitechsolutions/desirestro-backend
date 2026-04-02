-- V6: SUPER ADMIN FEATURES & AUDIT LOGGING
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- HELPER PROCEDURES (With Correct Delimiters)
-- =====================================================
DROP PROCEDURE IF EXISTS AddColumnUnlessExists;
DELIMITER //
CREATE PROCEDURE AddColumnUnlessExists(IN tableName VARCHAR(64), IN columnName VARCHAR(64), IN columnDefinition TEXT)
BEGIN
    IF NOT EXISTS (SELECT * FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tableName AND COLUMN_NAME = columnName) THEN
        SET @sql = CONCAT('ALTER TABLE ', tableName, ' ADD COLUMN ', columnName, ' ', columnDefinition);
        PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

DROP PROCEDURE IF EXISTS AddIndexUnlessExists;
DELIMITER //
CREATE PROCEDURE AddIndexUnlessExists(IN tableName VARCHAR(64), IN indexName VARCHAR(64), IN indexDefinition TEXT)
BEGIN
    IF NOT EXISTS (SELECT * FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = tableName AND index_name = indexName) THEN
        SET @sql = CONCAT('CREATE INDEX ', indexName, ' ON ', tableName, ' ', indexDefinition);
        PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

-- =====================================================
-- USERS TABLE UPDATES
-- =====================================================
CALL AddColumnUnlessExists('users', 'active', 'BOOLEAN NOT NULL DEFAULT TRUE');
CALL AddColumnUnlessExists('users', 'email', 'VARCHAR(150)');
CALL AddColumnUnlessExists('users', 'created_at', 'DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL AddColumnUnlessExists('users', 'updated_at', 'DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');

-- Safe Index Creation
CALL AddIndexUnlessExists('users', 'idx_users_restaurant', '(restaurant_id)');
CALL AddIndexUnlessExists('users', 'idx_users_active', '(active)');
CALL AddIndexUnlessExists('users', 'idx_users_email', '(email)');

-- =====================================================
-- AUDIT LOG TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(100),
    restaurant_id BIGINT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id BIGINT,
    old_value TEXT,
    new_value TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_restaurant (restaurant_id),
    INDEX idx_audit_timestamp (timestamp),
    INDEX idx_audit_action (action),
    INDEX idx_audit_entity (entity_type, entity_id),
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_audit_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- SUPPORT TICKET TABLES
-- =====================================================
CREATE TABLE IF NOT EXISTS support_ticket (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_number VARCHAR(50) NOT NULL UNIQUE,
    restaurant_id BIGINT NOT NULL,
    created_by_user_id BIGINT NOT NULL,
    assigned_to_user_id BIGINT,
    subject VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    category VARCHAR(50),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    resolved_at DATETIME,
    INDEX idx_ticket_restaurant (restaurant_id),
    INDEX idx_ticket_status (status),
    CONSTRAINT fk_ticket_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE CASCADE,
    CONSTRAINT fk_ticket_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ticket_assigned_to FOREIGN KEY (assigned_to_user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS support_ticket_comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    comment TEXT NOT NULL,
    is_internal BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_comment_ticket (ticket_id),
    CONSTRAINT fk_comment_ticket FOREIGN KEY (ticket_id) REFERENCES support_ticket(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- SYSTEM SETTINGS & SUBSCRIPTIONS
-- =====================================================
CREATE TABLE IF NOT EXISTS system_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value TEXT,
    description VARCHAR(500),
    category VARCHAR(50),
    is_public BOOLEAN DEFAULT FALSE,
    updated_by_user_id BIGINT,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_settings_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS restaurant_subscription (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    restaurant_id BIGINT NOT NULL UNIQUE,
    plan_type VARCHAR(50) NOT NULL DEFAULT 'TRIAL',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    start_date DATE NOT NULL,
    end_date DATE,
    max_users INT DEFAULT 5,
    max_tables INT DEFAULT 10,
    features JSON,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_subscription_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- DEFAULT DATA
-- =====================================================
INSERT IGNORE INTO users (username, password, role, full_name, email, active, restaurant_id)
VALUES ('superadmin', '$2a$10$xQKhF5xQKhF5xQKhF5xQKuO8YvZ5xQKhF5xQKhF5xQKhF5xQKhF5x', 'SUPER_ADMIN', 'System Administrator', 'admin@desirestro.com', TRUE, NULL);

INSERT INTO system_settings (setting_key, setting_value, description, category, is_public)
VALUES
    ('app.name', 'DesiRestro', 'Application name', 'GENERAL', TRUE),
    ('app.version', '1.0.0', 'Application version', 'GENERAL', TRUE),
    ('support.email', 'support@desirestro.com', 'Support email address', 'SUPPORT', TRUE)
ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value);

-- =====================================================
-- CLEANUP
-- =====================================================
DROP PROCEDURE IF EXISTS AddColumnUnlessExists;
DROP PROCEDURE IF EXISTS AddIndexUnlessExists;
SET FOREIGN_KEY_CHECKS = 1;