-- V4: Multi-tenancy support
-- MySQL 9.4 compatible (no ADD COLUMN IF NOT EXISTS)
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- RESTAURANT (tenant root)
-- =====================================================
CREATE TABLE IF NOT EXISTS restaurant (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    address     VARCHAR(255),
    phone       VARCHAR(20),
    email       VARCHAR(150),
    gstin       VARCHAR(20),
    state       VARCHAR(100),
    gst_rate    DOUBLE NOT NULL DEFAULT 18.0,
    code        VARCHAR(100) NOT NULL,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_restaurant_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO restaurant (id, name, code, created_at, updated_at)
VALUES (1, 'Default Restaurant', 'default-restaurant-001', NOW(), NOW())
ON DUPLICATE KEY UPDATE id = id;

-- =====================================================
-- REFRESH TOKENS
-- =====================================================
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    token       VARCHAR(255) NOT NULL,
    username    VARCHAR(100) NOT NULL,
    expiry_date DATETIME NOT NULL,
    revoked     BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE KEY uk_refresh_token (token),
    INDEX idx_refresh_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- BILL
-- =====================================================
ALTER TABLE bill
    ADD COLUMN restaurant_id BIGINT NULL,
    ADD COLUMN created_at    DATETIME NULL,
    ADD COLUMN updated_at    DATETIME NULL;

UPDATE bill SET restaurant_id = 1, created_at = NOW(), updated_at = NOW();

ALTER TABLE bill
    MODIFY COLUMN restaurant_id BIGINT NOT NULL,
    MODIFY COLUMN created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY COLUMN updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ADD CONSTRAINT fk_bill_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT;

-- =====================================================
-- KOT
-- =====================================================
ALTER TABLE kot
    ADD COLUMN restaurant_id BIGINT NULL,
    ADD COLUMN created_at    DATETIME NULL,
    ADD COLUMN updated_at    DATETIME NULL;

UPDATE kot SET restaurant_id = 1, created_at = NOW(), updated_at = NOW();

ALTER TABLE kot DROP INDEX uk_kot_number;

ALTER TABLE kot
    MODIFY COLUMN restaurant_id BIGINT NOT NULL,
    MODIFY COLUMN created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY COLUMN updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ADD CONSTRAINT fk_kot_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT,
    ADD UNIQUE KEY uk_kot_number_restaurant (kot_number, restaurant_id);

-- =====================================================
-- PARTY
-- =====================================================
ALTER TABLE party
    ADD COLUMN restaurant_id BIGINT NULL,
    ADD COLUMN created_at    DATETIME NULL,
    ADD COLUMN updated_at    DATETIME NULL;

UPDATE party SET restaurant_id = 1, created_at = NOW(), updated_at = NOW();

ALTER TABLE party
    MODIFY COLUMN restaurant_id BIGINT NOT NULL,
    MODIFY COLUMN created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY COLUMN updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ADD CONSTRAINT fk_party_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT;

-- =====================================================
-- RESTAURANT TABLE
-- =====================================================
ALTER TABLE restaurant_table
    ADD COLUMN restaurant_id BIGINT NULL,
    ADD COLUMN created_at    DATETIME NULL,
    ADD COLUMN updated_at    DATETIME NULL;

UPDATE restaurant_table SET restaurant_id = 1, created_at = NOW(), updated_at = NOW();

ALTER TABLE restaurant_table DROP INDEX uk_table_number;

ALTER TABLE restaurant_table
    MODIFY COLUMN restaurant_id BIGINT NOT NULL,
    MODIFY COLUMN created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY COLUMN updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ADD CONSTRAINT fk_table_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT,
    ADD UNIQUE KEY uk_table_number_restaurant (table_number, restaurant_id);

-- =====================================================
-- MENU ITEM
-- =====================================================
ALTER TABLE menu_item
    ADD COLUMN restaurant_id BIGINT NULL,
    ADD COLUMN created_at    DATETIME NULL,
    ADD COLUMN updated_at    DATETIME NULL;

UPDATE menu_item SET restaurant_id = 1, created_at = NOW(), updated_at = NOW();

ALTER TABLE menu_item
    MODIFY COLUMN restaurant_id BIGINT NOT NULL,
    MODIFY COLUMN created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY COLUMN updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ADD CONSTRAINT fk_menu_item_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT;

-- =====================================================
-- CATEGORY
-- =====================================================
ALTER TABLE category
    ADD COLUMN restaurant_id BIGINT NULL,
    ADD COLUMN created_at    DATETIME NULL,
    ADD COLUMN updated_at    DATETIME NULL;

UPDATE category SET restaurant_id = 1, created_at = NOW(), updated_at = NOW();

ALTER TABLE category DROP INDEX uk_category_name;

ALTER TABLE category
    MODIFY COLUMN restaurant_id BIGINT NOT NULL,
    MODIFY COLUMN created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY COLUMN updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ADD CONSTRAINT fk_category_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT,
    ADD UNIQUE KEY uk_category_name_restaurant (name, restaurant_id);

-- =====================================================
-- INGREDIENT
-- =====================================================
ALTER TABLE ingredient
    ADD COLUMN restaurant_id BIGINT NULL,
    ADD COLUMN created_at    DATETIME NULL,
    ADD COLUMN updated_at    DATETIME NULL;

UPDATE ingredient SET restaurant_id = 1, created_at = NOW(), updated_at = NOW();

ALTER TABLE ingredient DROP INDEX uk_ingredient_name;

ALTER TABLE ingredient
    MODIFY COLUMN restaurant_id BIGINT NOT NULL,
    MODIFY COLUMN created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY COLUMN updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ADD CONSTRAINT fk_ingredient_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT,
    ADD UNIQUE KEY uk_ingredient_name_restaurant (name, restaurant_id);

-- =====================================================
-- LEAVE REQUEST
-- =====================================================
ALTER TABLE leave_request
    ADD COLUMN restaurant_id BIGINT NULL,
    ADD COLUMN created_at    DATETIME NULL,
    ADD COLUMN updated_at    DATETIME NULL;

UPDATE leave_request SET restaurant_id = 1, created_at = NOW(), updated_at = NOW();

ALTER TABLE leave_request
    MODIFY COLUMN restaurant_id BIGINT NOT NULL,
    MODIFY COLUMN created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY COLUMN updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ADD CONSTRAINT fk_leave_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT;

-- =====================================================
-- ATTENDANCE
-- =====================================================
ALTER TABLE attendance
    ADD COLUMN restaurant_id BIGINT NULL,
    ADD COLUMN created_at    DATETIME NULL,
    ADD COLUMN updated_at    DATETIME NULL;

UPDATE attendance SET restaurant_id = 1, created_at = NOW(), updated_at = NOW();

ALTER TABLE attendance
    MODIFY COLUMN restaurant_id BIGINT NOT NULL,
    MODIFY COLUMN created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY COLUMN updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ADD CONSTRAINT fk_attendance_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT;

-- =====================================================
-- STAFF
-- =====================================================
ALTER TABLE staff
    ADD COLUMN restaurant_id BIGINT NULL,
    ADD COLUMN created_at    DATETIME NULL,
    ADD COLUMN updated_at    DATETIME NULL;

UPDATE staff SET restaurant_id = 1, created_at = NOW(), updated_at = NOW();

ALTER TABLE staff
    MODIFY COLUMN restaurant_id BIGINT NOT NULL,
    MODIFY COLUMN created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY COLUMN updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ADD CONSTRAINT fk_staff_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT;

-- =====================================================
-- USERS
-- =====================================================
ALTER TABLE users
    ADD COLUMN restaurant_id BIGINT NULL,
    ADD COLUMN active        BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN created_at    DATETIME NULL,
    ADD COLUMN updated_at    DATETIME NULL;

UPDATE users SET restaurant_id = 1, active = TRUE, created_at = NOW(), updated_at = NOW();
UPDATE users SET role = 'OWNER' WHERE username = 'admin' AND role = 'ADMIN';

ALTER TABLE users
    MODIFY COLUMN created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY COLUMN updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ADD CONSTRAINT fk_users_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT;

SET FOREIGN_KEY_CHECKS = 1;
