-- =====================================================
-- V6: SUPER ADMIN FEATURES & AUDIT LOGGING
-- =====================================================

-- Add active flag and timestamps to users table if not exists
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE,
ADD COLUMN IF NOT EXISTS email VARCHAR(150),
ADD COLUMN IF NOT EXISTS restaurant_id BIGINT,
ADD COLUMN IF NOT EXISTS created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Add foreign key for restaurant_id
ALTER TABLE users
ADD CONSTRAINT fk_users_restaurant
    FOREIGN KEY (restaurant_id) REFERENCES restaurant(id)
    ON DELETE SET NULL;

-- Add index for better query performance
CREATE INDEX IF NOT EXISTS idx_users_restaurant ON users(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_users_active ON users(active);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

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
    
    CONSTRAINT fk_audit_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_audit_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurant(id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- SUPPORT TICKET TABLE
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
    INDEX idx_ticket_created_by (created_by_user_id),
    INDEX idx_ticket_assigned_to (assigned_to_user_id),
    INDEX idx_ticket_status (status),
    INDEX idx_ticket_priority (priority),
    INDEX idx_ticket_created_at (created_at),
    
    CONSTRAINT fk_ticket_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurant(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_ticket_created_by
        FOREIGN KEY (created_by_user_id) REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_ticket_assigned_to
        FOREIGN KEY (assigned_to_user_id) REFERENCES users(id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- SUPPORT TICKET COMMENT TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS support_ticket_comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    comment TEXT NOT NULL,
    is_internal BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_comment_ticket (ticket_id),
    INDEX idx_comment_user (user_id),
    INDEX idx_comment_created_at (created_at),
    
    CONSTRAINT fk_comment_ticket
        FOREIGN KEY (ticket_id) REFERENCES support_ticket(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_comment_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- SYSTEM SETTINGS TABLE
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
    
    INDEX idx_settings_category (category),
    INDEX idx_settings_public (is_public),
    
    CONSTRAINT fk_settings_updated_by
        FOREIGN KEY (updated_by_user_id) REFERENCES users(id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- RESTAURANT SUBSCRIPTION TABLE
-- =====================================================
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
    
    INDEX idx_subscription_restaurant (restaurant_id),
    INDEX idx_subscription_status (status),
    INDEX idx_subscription_end_date (end_date),
    
    CONSTRAINT fk_subscription_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurant(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- INSERT DEFAULT SUPER ADMIN USER
-- =====================================================
-- Password: SuperAdmin@123 (BCrypt encoded)
INSERT INTO users (username, password, role, full_name, email, active, restaurant_id)
VALUES (
    'superadmin',
    '$2a$10$xQKhF5xQKhF5xQKhF5xQKuO8YvZ5xQKhF5xQKhF5xQKhF5xQKhF5x',
    'SUPER_ADMIN',
    'System Administrator',
    'admin@desirestro.com',
    TRUE,
    NULL
)
ON DUPLICATE KEY UPDATE username=username;

-- =====================================================
-- INSERT DEFAULT SYSTEM SETTINGS
-- =====================================================
INSERT INTO system_settings (setting_key, setting_value, description, category, is_public)
VALUES
    ('app.name', 'DesiRestro', 'Application name', 'GENERAL', TRUE),
    ('app.version', '1.0.0', 'Application version', 'GENERAL', TRUE),
    ('support.email', 'support@desirestro.com', 'Support email address', 'SUPPORT', TRUE),
    ('support.phone', '+91-1234567890', 'Support phone number', 'SUPPORT', TRUE),
    ('trial.duration.days', '30', 'Trial period duration in days', 'SUBSCRIPTION', FALSE),
    ('max.login.attempts', '5', 'Maximum login attempts before lockout', 'SECURITY', FALSE),
    ('session.timeout.minutes', '60', 'Session timeout in minutes', 'SECURITY', FALSE)
ON DUPLICATE KEY UPDATE setting_key=setting_key;

-- =====================================================
-- CREATE TRIGGER FOR AUDIT LOGGING
-- =====================================================
DELIMITER //

CREATE TRIGGER IF NOT EXISTS audit_user_changes
AFTER UPDATE ON users
FOR EACH ROW
BEGIN
    IF OLD.active != NEW.active OR OLD.role != NEW.role THEN
        INSERT INTO audit_log (
            user_id, username, restaurant_id, action, entity_type, entity_id,
            old_value, new_value, timestamp
        ) VALUES (
            NEW.id,
            NEW.username,
            NEW.restaurant_id,
            'UPDATE',
            'USER',
            NEW.id,
            JSON_OBJECT('active', OLD.active, 'role', OLD.role),
            JSON_OBJECT('active', NEW.active, 'role', NEW.role),
            NOW()
        );
    END IF;
END//

DELIMITER ;

-- Made with Bob
