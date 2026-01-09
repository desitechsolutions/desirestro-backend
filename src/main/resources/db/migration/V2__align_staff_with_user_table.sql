-- V2__align_staff_with_user_table.sql
-- Align staff table with users table (MySQL-safe & re-runnable)

-- 1️⃣ Add user_id column only if missing
SET @col_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'staff'
      AND COLUMN_NAME = 'user_id'
);

SET @sql := IF(
    @col_exists = 0,
    'ALTER TABLE staff ADD COLUMN user_id BIGINT',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2️⃣ Drop legacy auth columns if present
SET @drop_cols := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'staff'
      AND COLUMN_NAME IN ('username','password','role')
);

SET @sql := IF(
    @drop_cols > 0,
    'ALTER TABLE staff
        DROP COLUMN username,
        DROP COLUMN password,
        DROP COLUMN role',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3️⃣ Drop legacy indexes if present
SET @idx_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'staff'
      AND INDEX_NAME IN ('uk_staff_username','idx_staff_role')
);

SET @sql := IF(
    @idx_exists > 0,
    'ALTER TABLE staff
        DROP INDEX uk_staff_username,
        DROP INDEX idx_staff_role',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4️⃣ Add foreign key only if missing
SET @fk_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'staff'
      AND CONSTRAINT_NAME = 'fk_staff_user'
);

SET @sql := IF(
    @fk_exists = 0,
    'ALTER TABLE staff
        ADD CONSTRAINT fk_staff_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5️⃣ Add one-to-one uniqueness
SET @uk_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'staff'
      AND INDEX_NAME = 'uk_staff_user'
);

SET @sql := IF(
    @uk_exists = 0,
    'ALTER TABLE staff ADD CONSTRAINT uk_staff_user UNIQUE (user_id)',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
