-- V5: Add email to users + password reset tokens table

-- Add email column to users (nullable for backward compat)
ALTER TABLE users ADD COLUMN email VARCHAR(150) NULL;

-- Password reset tokens
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    token       VARCHAR(255) NOT NULL,
    user_id     BIGINT NOT NULL,
    restaurant_id BIGINT NOT NULL,
    expiry_date DATETIME NOT NULL,
    used        BOOLEAN NOT NULL DEFAULT FALSE,
    used_at     DATETIME NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_prt_token (token),
    INDEX idx_prt_user (user_id),
    CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_prt_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
