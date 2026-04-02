-- V4: Multi-tenancy support with Idempotency Helpers
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
-- APPLY MULTI-TENANCY TO EXISTING TABLES
-- =====================================================

-- 1. BILL
CALL AddColumnUnlessExists('bill', 'restaurant_id', 'BIGINT NULL');
CALL AddColumnUnlessExists('bill', 'created_at', 'DATETIME NULL');
CALL AddColumnUnlessExists('bill', 'updated_at', 'DATETIME NULL');
UPDATE bill SET restaurant_id = 1, created_at = NOW(), updated_at = NOW() WHERE restaurant_id IS NULL;
ALTER TABLE bill MODIFY COLUMN restaurant_id BIGINT NOT NULL, MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, MODIFY COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- 2. KOT
CALL AddColumnUnlessExists('kot', 'restaurant_id', 'BIGINT NULL');
CALL AddColumnUnlessExists('kot', 'created_at', 'DATETIME NULL');
CALL AddColumnUnlessExists('kot', 'updated_at', 'DATETIME NULL');
UPDATE kot SET restaurant_id = 1, created_at = NOW(), updated_at = NOW() WHERE restaurant_id IS NULL;
ALTER TABLE kot MODIFY COLUMN restaurant_id BIGINT NOT NULL, MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, MODIFY COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- 3. PARTY
CALL AddColumnUnlessExists('party', 'restaurant_id', 'BIGINT NULL');
CALL AddColumnUnlessExists('party', 'created_at', 'DATETIME NULL');
CALL AddColumnUnlessExists('party', 'updated_at', 'DATETIME NULL');
UPDATE party SET restaurant_id = 1, created_at = NOW(), updated_at = NOW() WHERE restaurant_id IS NULL;
ALTER TABLE party MODIFY COLUMN restaurant_id BIGINT NOT NULL, MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, MODIFY COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- 4. RESTAURANT_TABLE
CALL AddColumnUnlessExists('restaurant_table', 'restaurant_id', 'BIGINT NULL');
CALL AddColumnUnlessExists('restaurant_table', 'created_at', 'DATETIME NULL');
CALL AddColumnUnlessExists('restaurant_table', 'updated_at', 'DATETIME NULL');
UPDATE restaurant_table SET restaurant_id = 1, created_at = NOW(), updated_at = NOW() WHERE restaurant_id IS NULL;
ALTER TABLE restaurant_table MODIFY COLUMN restaurant_id BIGINT NOT NULL, MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, MODIFY COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- 5. MENU_ITEM
CALL AddColumnUnlessExists('menu_item', 'restaurant_id', 'BIGINT NULL');
CALL AddColumnUnlessExists('menu_item', 'created_at', 'DATETIME NULL');
CALL AddColumnUnlessExists('menu_item', 'updated_at', 'DATETIME NULL');
UPDATE menu_item SET restaurant_id = 1, created_at = NOW(), updated_at = NOW() WHERE restaurant_id IS NULL;
ALTER TABLE menu_item MODIFY COLUMN restaurant_id BIGINT NOT NULL, MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, MODIFY COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- 6. CATEGORY
CALL AddColumnUnlessExists('category', 'restaurant_id', 'BIGINT NULL');
CALL AddColumnUnlessExists('category', 'created_at', 'DATETIME NULL');
CALL AddColumnUnlessExists('category', 'updated_at', 'DATETIME NULL');
UPDATE category SET restaurant_id = 1, created_at = NOW(), updated_at = NOW() WHERE restaurant_id IS NULL;
ALTER TABLE category MODIFY COLUMN restaurant_id BIGINT NOT NULL, MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, MODIFY COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- 7. INGREDIENT
CALL AddColumnUnlessExists('ingredient', 'restaurant_id', 'BIGINT NULL');
CALL AddColumnUnlessExists('ingredient', 'created_at', 'DATETIME NULL');
CALL AddColumnUnlessExists('ingredient', 'updated_at', 'DATETIME NULL');
UPDATE ingredient SET restaurant_id = 1, created_at = NOW(), updated_at = NOW() WHERE restaurant_id IS NULL;
ALTER TABLE ingredient MODIFY COLUMN restaurant_id BIGINT NOT NULL, MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, MODIFY COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- 8. LEAVE_REQUEST
CALL AddColumnUnlessExists('leave_request', 'restaurant_id', 'BIGINT NULL');
CALL AddColumnUnlessExists('leave_request', 'created_at', 'DATETIME NULL');
CALL AddColumnUnlessExists('leave_request', 'updated_at', 'DATETIME NULL');
UPDATE leave_request SET restaurant_id = 1, created_at = NOW(), updated_at = NOW() WHERE restaurant_id IS NULL;
ALTER TABLE leave_request MODIFY COLUMN restaurant_id BIGINT NOT NULL, MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, MODIFY COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- 9. ATTENDANCE
CALL AddColumnUnlessExists('attendance', 'restaurant_id', 'BIGINT NULL');
CALL AddColumnUnlessExists('attendance', 'created_at', 'DATETIME NULL');
CALL AddColumnUnlessExists('attendance', 'updated_at', 'DATETIME NULL');
UPDATE attendance SET restaurant_id = 1, created_at = NOW(), updated_at = NOW() WHERE restaurant_id IS NULL;
ALTER TABLE attendance MODIFY COLUMN restaurant_id BIGINT NOT NULL, MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, MODIFY COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- 10. STAFF
CALL AddColumnUnlessExists('staff', 'restaurant_id', 'BIGINT NULL');
CALL AddColumnUnlessExists('staff', 'created_at', 'DATETIME NULL');
CALL AddColumnUnlessExists('staff', 'updated_at', 'DATETIME NULL');
UPDATE staff SET restaurant_id = 1, created_at = NOW(), updated_at = NOW() WHERE restaurant_id IS NULL;
ALTER TABLE staff MODIFY COLUMN restaurant_id BIGINT NOT NULL, MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, MODIFY COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- 11. USERS
CALL AddColumnUnlessExists('users', 'restaurant_id', 'BIGINT NULL');
CALL AddColumnUnlessExists('users', 'active', 'BOOLEAN NOT NULL DEFAULT TRUE');
CALL AddColumnUnlessExists('users', 'created_at', 'DATETIME NULL');
CALL AddColumnUnlessExists('users', 'updated_at', 'DATETIME NULL');
UPDATE users SET restaurant_id = 1, active = TRUE, created_at = NOW(), updated_at = NOW() WHERE restaurant_id IS NULL;
UPDATE users SET role = 'OWNER' WHERE username = 'admin' AND role = 'ADMIN';
ALTER TABLE users MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, MODIFY COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- =====================================================
-- DROP OLD UNIQUE INDEXES & ADD MULTI-TENANT CONSTRAINTS
-- =====================================================

-- Helper to safely drop index if exists
DROP PROCEDURE IF EXISTS DropIndexIfExists;
DELIMITER //
CREATE PROCEDURE DropIndexIfExists(IN tableName VARCHAR(64), IN indexName VARCHAR(64))
BEGIN
    IF EXISTS (SELECT * FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = tableName AND index_name = indexName) THEN
        SET @sql = CONCAT('ALTER TABLE ', tableName, ' DROP INDEX ', indexName);
        PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

CALL DropIndexIfExists('kot', 'uk_kot_number');
CALL DropIndexIfExists('restaurant_table', 'uk_table_number');
CALL DropIndexIfExists('category', 'uk_category_name');
CALL DropIndexIfExists('ingredient', 'uk_ingredient_name');

-- Add Foreign Keys & New Unique Keys (Wrapped in checks to avoid duplicates)
DROP PROCEDURE IF EXISTS AddConstraintUnlessExists;
DELIMITER //
CREATE PROCEDURE AddConstraintUnlessExists(IN tableName VARCHAR(64), IN constraintName VARCHAR(64), IN constraintDef TEXT)
BEGIN
    IF NOT EXISTS (SELECT * FROM information_schema.table_constraints WHERE table_schema = DATABASE() AND table_name = tableName AND constraint_name = constraintName) THEN
        SET @sql = CONCAT('ALTER TABLE ', tableName, ' ADD ', constraintDef);
        PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

CALL AddConstraintUnlessExists('bill', 'fk_bill_restaurant', 'CONSTRAINT fk_bill_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT');
CALL AddConstraintUnlessExists('kot', 'fk_kot_restaurant', 'CONSTRAINT fk_kot_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT');
CALL AddConstraintUnlessExists('kot', 'uk_kot_number_restaurant', 'UNIQUE KEY uk_kot_number_restaurant (kot_number, restaurant_id)');
CALL AddConstraintUnlessExists('party', 'fk_party_restaurant', 'CONSTRAINT fk_party_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT');
CALL AddConstraintUnlessExists('restaurant_table', 'fk_table_restaurant', 'CONSTRAINT fk_table_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT');
CALL AddConstraintUnlessExists('restaurant_table', 'uk_table_number_restaurant', 'UNIQUE KEY uk_table_number_restaurant (table_number, restaurant_id)');
CALL AddConstraintUnlessExists('menu_item', 'fk_menu_item_restaurant', 'CONSTRAINT fk_menu_item_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT');
CALL AddConstraintUnlessExists('category', 'fk_category_restaurant', 'CONSTRAINT fk_category_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT');
CALL AddConstraintUnlessExists('category', 'uk_category_name_restaurant', 'UNIQUE KEY uk_category_name_restaurant (name, restaurant_id)');
CALL AddConstraintUnlessExists('ingredient', 'fk_ingredient_restaurant', 'CONSTRAINT fk_ingredient_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT');
CALL AddConstraintUnlessExists('ingredient', 'uk_ingredient_name_restaurant', 'UNIQUE KEY uk_ingredient_name_restaurant (name, restaurant_id)');
CALL AddConstraintUnlessExists('leave_request', 'fk_leave_restaurant', 'CONSTRAINT fk_leave_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT');
CALL AddConstraintUnlessExists('attendance', 'fk_attendance_restaurant', 'CONSTRAINT fk_attendance_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT');
CALL AddConstraintUnlessExists('staff', 'fk_staff_restaurant', 'CONSTRAINT fk_staff_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT');
CALL AddConstraintUnlessExists('users', 'fk_users_restaurant', 'CONSTRAINT fk_users_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE RESTRICT');

-- Clean up
DROP PROCEDURE IF EXISTS AddColumnUnlessExists;
DROP PROCEDURE IF EXISTS DropIndexIfExists;
DROP PROCEDURE IF EXISTS AddConstraintUnlessExists;

SET FOREIGN_KEY_CHECKS = 1;