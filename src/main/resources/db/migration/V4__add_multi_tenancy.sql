-- =====================================================
-- V4: Multi-tenancy — Add restaurant tenant table
--     and add restaurant_id FK to all domain tables.
--
-- Migration strategy:
--   1. Create the restaurant + refresh_tokens tables.
--   2. Insert a default restaurant for all existing seed data.
--   3. Add restaurant_id + audit columns (nullable first).
--   4. Backfill existing rows with the default restaurant.
--   5. Enforce NOT NULL, FK constraints, and scoped unique keys.
-- Note: ordered bottom-up so child tables are modified before
--       FK constraints are added, avoiding FK check disabling.
-- =====================================================

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
    UNIQUE KEY uk_restaurant_code (code),
    INDEX idx_restaurant_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert the default restaurant that owns all existing seed data
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
-- BILL — add restaurant_id + audit columns (leaf table first)
-- =====================================================
ALTER TABLE bill
    ADD COLUMN IF NOT EXISTS restaurant_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS created_at    DATETIME,
    ADD COLUMN IF NOT EXISTS updated_at    DATETIME;

UPDATE bill SET restaurant_id = 1, created_at = NOW(), updated_at = NOW() WHERE restaurant_id IS NULL;

ALTER TABLE bill
    MODIFY COLUMN restaurant_id BIGINT NOT NULL,
    MODIFY COLUMN created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY COLUMN updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE bill
    ADD CONSTRAINT IF NOT EXISTS fk_bill_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT;

-- =====================================================
-- KOT ITEMS (child of kot — audit only via kot)
-- =====================================================

-- =====================================================
-- KOT — add restaurant_id + audit columns
-- =====================================================
ALTER TABLE kot
    ADD COLUMN IF NOT EXISTS restaurant_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS created_at    DATETIME,
    ADD COLUMN IF NOT EXISTS updated_at    DATETIME;

UPDATE kot SET restaurant_id = 1, created_at = NOW(), updated_at = NOW() WHERE restaurant_id IS NULL;

ALTER TABLE kot
    MODIFY COLUMN restaurant_id BIGINT NOT NULL,
    MODIFY COLUMN created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY COLUMN updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Drop old global unique; add per-restaurant unique
ALTER TABLE kot
    DROP INDEX IF EXISTS uk_kot_number;

ALTER TABLE kot
    ADD CONSTRAINT IF NOT EXISTS fk_kot_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT,
    ADD UNIQUE KEY IF NOT EXISTS uk_kot_number_restaurant (kot_number, restaurant_id);

-- =====================================================
-- PARTY — add restaurant_id + audit columns
-- =====================================================
ALTER TABLE party
    ADD COLUMN IF NOT EXISTS restaurant_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS created_at    DATETIME,
    ADD COLUMN IF NOT EXISTS updated_at    DATETIME;

UPDATE party SET restaurant_id = 1, created_at = NOW(), updated_at = NOW() WHERE restaurant_id IS NULL;

ALTER TABLE party
    MODIFY COLUMN restaurant_id BIGINT NOT NULL,
    MODIFY COLUMN created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY COLUMN updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE party
    ADD CONSTRAINT IF NOT EXISTS fk_party_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT;

-- =====================================================
-- RESTAURANT TABLE — add restaurant_id + audit columns
-- =====================================================
ALTER TABLE restaurant_table
    ADD COLUMN IF NOT EXISTS restaurant_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS created_at    DATETIME,
    ADD COLUMN IF NOT EXISTS updated_at    DATETIME;

UPDATE restaurant_table SET restaurant_id = 1, created_at = NOW(), updated_at = NOW() WHERE restaurant_id IS NULL;

ALTER TABLE restaurant_table
    MODIFY COLUMN restaurant_id BIGINT NOT NULL,
    MODIFY COLUMN created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY COLUMN updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Drop old global unique; add per-restaurant unique
ALTER TABLE restaurant_table
    DROP INDEX IF EXISTS uk_table_number;

ALTER TABLE restaurant_table
    ADD CONSTRAINT IF NOT EXISTS fk_table_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT,
    ADD UNIQUE KEY IF NOT EXISTS uk_table_number_restaurant (table_number, restaurant_id);

-- =====================================================
-- MENU ITEM INGREDIENT (junction table — no restaurant_id needed)
-- =====================================================

-- =====================================================
-- MENU ITEM — add restaurant_id + audit columns
-- =====================================================
ALTER TABLE menu_item
    ADD COLUMN IF NOT EXISTS restaurant_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS created_at    DATETIME,
    ADD COLUMN IF NOT EXISTS updated_at    DATETIME;

UPDATE menu_item SET restaurant_id = 1, created_at = NOW(), updated_at = NOW() WHERE restaurant_id IS NULL;

ALTER TABLE menu_item
    MODIFY COLUMN restaurant_id BIGINT NOT NULL,
    MODIFY COLUMN created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY COLUMN updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE menu_item
    ADD CONSTRAINT IF NOT EXISTS fk_menu_item_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT;

-- =====================================================
-- CATEGORY — add restaurant_id + audit columns
-- =====================================================
ALTER TABLE category
    ADD COLUMN IF NOT EXISTS restaurant_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS created_at    DATETIME,
    ADD COLUMN IF NOT EXISTS updated_at    DATETIME;

UPDATE category SET restaurant_id = 1, created_at = NOW(), updated_at = NOW() WHERE restaurant_id IS NULL;

ALTER TABLE category
    MODIFY COLUMN restaurant_id BIGINT NOT NULL,
    MODIFY COLUMN created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY COLUMN updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE category
    DROP INDEX IF EXISTS uk_category_name;

ALTER TABLE category
    ADD CONSTRAINT IF NOT EXISTS fk_category_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT,
    ADD UNIQUE KEY IF NOT EXISTS uk_category_name_restaurant (name, restaurant_id);

-- =====================================================
-- INGREDIENT — add restaurant_id + audit columns
-- =====================================================
ALTER TABLE ingredient
    ADD COLUMN IF NOT EXISTS restaurant_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS created_at    DATETIME,
    ADD COLUMN IF NOT EXISTS updated_at    DATETIME;

UPDATE ingredient SET restaurant_id = 1, created_at = NOW(), updated_at = NOW() WHERE restaurant_id IS NULL;

ALTER TABLE ingredient
    MODIFY COLUMN restaurant_id BIGINT NOT NULL,
    MODIFY COLUMN created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY COLUMN updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE ingredient
    DROP INDEX IF EXISTS uk_ingredient_name;

ALTER TABLE ingredient
    ADD CONSTRAINT IF NOT EXISTS fk_ingredient_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT,
    ADD UNIQUE KEY IF NOT EXISTS uk_ingredient_name_restaurant (name, restaurant_id);

-- =====================================================
-- LEAVE REQUEST — add restaurant_id + audit columns
-- =====================================================
ALTER TABLE leave_request
    ADD COLUMN IF NOT EXISTS restaurant_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS created_at    DATETIME,
    ADD COLUMN IF NOT EXISTS updated_at    DATETIME;

UPDATE leave_request SET restaurant_id = 1, created_at = NOW(), updated_at = NOW() WHERE restaurant_id IS NULL;

ALTER TABLE leave_request
    MODIFY COLUMN restaurant_id BIGINT NOT NULL,
    MODIFY COLUMN created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY COLUMN updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE leave_request
    ADD CONSTRAINT IF NOT EXISTS fk_leave_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT;

-- =====================================================
-- ATTENDANCE — add restaurant_id + audit columns
-- =====================================================
ALTER TABLE attendance
    ADD COLUMN IF NOT EXISTS restaurant_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS created_at    DATETIME,
    ADD COLUMN IF NOT EXISTS updated_at    DATETIME;

UPDATE attendance SET restaurant_id = 1, created_at = NOW(), updated_at = NOW() WHERE restaurant_id IS NULL;

ALTER TABLE attendance
    MODIFY COLUMN restaurant_id BIGINT NOT NULL,
    MODIFY COLUMN created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY COLUMN updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE attendance
    ADD CONSTRAINT IF NOT EXISTS fk_attendance_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT;

-- =====================================================
-- STAFF — add restaurant_id + audit columns
-- =====================================================
ALTER TABLE staff
    ADD COLUMN IF NOT EXISTS restaurant_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS created_at    DATETIME,
    ADD COLUMN IF NOT EXISTS updated_at    DATETIME;

UPDATE staff SET restaurant_id = 1, created_at = NOW(), updated_at = NOW() WHERE restaurant_id IS NULL;

ALTER TABLE staff
    MODIFY COLUMN restaurant_id BIGINT NOT NULL,
    MODIFY COLUMN created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY COLUMN updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE staff
    ADD CONSTRAINT IF NOT EXISTS fk_staff_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT;

-- =====================================================
-- USERS — add restaurant_id + active + audit columns
-- =====================================================
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS restaurant_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS active        BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS created_at    DATETIME,
    ADD COLUMN IF NOT EXISTS updated_at    DATETIME;

UPDATE users SET restaurant_id = 1, active = TRUE, created_at = NOW(), updated_at = NOW()
WHERE restaurant_id IS NULL;

-- Promote existing admin user to OWNER role
UPDATE users SET role = 'OWNER' WHERE username = 'admin' AND role = 'ADMIN';

ALTER TABLE users
    MODIFY COLUMN created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY COLUMN updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE users
    ADD CONSTRAINT IF NOT EXISTS fk_users_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT;

