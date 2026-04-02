-- V7: Indian restaurant billing features (GST, Jain, Spice Levels)
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
-- KOT: add order-level fields
-- =====================================================
CALL AddColumnUnlessExists('kot', 'order_type', "VARCHAR(20) DEFAULT 'DINE_IN'");
CALL AddColumnUnlessExists('kot', 'customer_name', "VARCHAR(100)");
CALL AddColumnUnlessExists('kot', 'customer_phone', "VARCHAR(20)");
CALL AddColumnUnlessExists('kot', 'customer_address', "TEXT");
CALL AddColumnUnlessExists('kot', 'delivery_address', "TEXT");
CALL AddColumnUnlessExists('kot', 'special_instructions', "TEXT");

-- =====================================================
-- KOT_ITEMS: add item-level Indian features
-- =====================================================
CALL AddColumnUnlessExists('kot_items', 'spice_level', "VARCHAR(20)");
CALL AddColumnUnlessExists('kot_items', 'special_instructions', "TEXT");
CALL AddColumnUnlessExists('kot_items', 'is_jain', "BOOLEAN DEFAULT FALSE");
CALL AddColumnUnlessExists('kot_items', 'hsn_code', "VARCHAR(20)");

-- =====================================================
-- MENU_ITEM: add Indian features
-- =====================================================
CALL AddColumnUnlessExists('menu_item', 'spice_level', "VARCHAR(20) DEFAULT 'MEDIUM'");
CALL AddColumnUnlessExists('menu_item', 'is_jain', "BOOLEAN DEFAULT FALSE");
CALL AddColumnUnlessExists('menu_item', 'is_swaminarayan', "BOOLEAN DEFAULT FALSE");
CALL AddColumnUnlessExists('menu_item', 'hsn_code', "VARCHAR(20)");
CALL AddColumnUnlessExists('menu_item', 'preparation_time', "INT DEFAULT 15");

-- =====================================================
-- CUSTOMER: loyalty & GST-aware customer master
-- =====================================================
CREATE TABLE IF NOT EXISTS customer (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    restaurant_id   BIGINT NOT NULL,
    name            VARCHAR(100) NOT NULL,
    phone           VARCHAR(20) NOT NULL,
    email           VARCHAR(100),
    gstin           VARCHAR(15),
    address         TEXT,
    city            VARCHAR(50),
    state           VARCHAR(50),
    pincode         VARCHAR(10),
    credit_limit    DECIMAL(10,2) DEFAULT 0.00,
    credit_balance  DECIMAL(10,2) DEFAULT 0.00,
    loyalty_points  INT DEFAULT 0,
    total_orders    INT DEFAULT 0,
    total_spent     DECIMAL(10,2) DEFAULT 0.00,
    is_active       BOOLEAN DEFAULT TRUE,
    notes           TEXT,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_customer_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE CASCADE,
    UNIQUE KEY uk_customer_phone (restaurant_id, phone),
    INDEX idx_customer_phone (phone),
    INDEX idx_customer_name  (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- BILL: drop old V1 schema and create the correct one
-- =====================================================
DROP TABLE IF EXISTS bill;

CREATE TABLE bill (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    restaurant_id           BIGINT NOT NULL,
    bill_number             VARCHAR(50) NOT NULL,
    order_id                BIGINT NOT NULL,
    customer_id             BIGINT,
    table_number            INT,
    order_type              VARCHAR(20) DEFAULT 'DINE_IN',
    subtotal                DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    taxable_amount          DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    tax_type                VARCHAR(20) DEFAULT 'CGST_SGST',
    cgst_rate               DECIMAL(5,2) DEFAULT 9.00,
    sgst_rate               DECIMAL(5,2) DEFAULT 9.00,
    igst_rate               DECIMAL(5,2) DEFAULT 0.00,
    cgst_amount             DECIMAL(10,2) DEFAULT 0.00,
    sgst_amount             DECIMAL(10,2) DEFAULT 0.00,
    igst_amount             DECIMAL(10,2) DEFAULT 0.00,
    total_tax               DECIMAL(10,2) DEFAULT 0.00,
    service_charge_rate     DECIMAL(5,2) DEFAULT 10.00,
    service_charge_amount   DECIMAL(10,2) DEFAULT 0.00,
    packaging_charges       DECIMAL(10,2) DEFAULT 0.00,
    delivery_charges        DECIMAL(10,2) DEFAULT 0.00,
    discount_rate           DECIMAL(5,2) DEFAULT 0.00,
    discount_amount         DECIMAL(10,2) DEFAULT 0.00,
    discount_reason         VARCHAR(200),
    total_before_round_off  DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    round_off_amount        DECIMAL(10,2) DEFAULT 0.00,
    grand_total             DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    payment_method          VARCHAR(20) DEFAULT 'CASH',
    payment_reference       VARCHAR(100),
    paid_amount             DECIMAL(10,2) DEFAULT 0.00,
    change_amount           DECIMAL(10,2) DEFAULT 0.00,
    captain_id              BIGINT,
    cashier_id              BIGINT,
    is_paid                 BOOLEAN DEFAULT FALSE,
    is_cancelled            BOOLEAN DEFAULT FALSE,
    cancellation_reason     TEXT,
    bill_time               DATETIME DEFAULT CURRENT_TIMESTAMP,
    payment_time            DATETIME NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_bill_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE CASCADE,
    CONSTRAINT fk_bill_customer   FOREIGN KEY (customer_id)   REFERENCES customer(id)   ON DELETE SET NULL,
    CONSTRAINT fk_bill_captain    FOREIGN KEY (captain_id)    REFERENCES users(id)      ON DELETE SET NULL,
    CONSTRAINT fk_bill_cashier    FOREIGN KEY (cashier_id)    REFERENCES users(id)      ON DELETE SET NULL,
    UNIQUE KEY uk_bill_number (restaurant_id, bill_number),
    INDEX idx_bill_time           (bill_time),
    INDEX idx_bill_status         (is_paid, is_cancelled),
    INDEX idx_bill_payment_method (payment_method)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- BILL_ITEM: line items for each bill
-- =====================================================
CREATE TABLE IF NOT EXISTS bill_item (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_id               BIGINT NOT NULL,
    menu_item_id          BIGINT NOT NULL,
    item_name             VARCHAR(100) NOT NULL,
    item_code             VARCHAR(50),
    category              VARCHAR(50),
    quantity              INT NOT NULL,
    unit_price            DECIMAL(10,2) NOT NULL,
    item_total            DECIMAL(10,2) NOT NULL,
    spice_level           VARCHAR(20),
    special_instructions  TEXT,
    is_veg                BOOLEAN DEFAULT TRUE,
    is_jain               BOOLEAN DEFAULT FALSE,
    hsn_code              VARCHAR(20),
    created_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bill_item_bill FOREIGN KEY (bill_id) REFERENCES bill(id) ON DELETE CASCADE,
    INDEX idx_bill_item_bill (bill_id),
    INDEX idx_bill_item_menu (menu_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- DAILY_SALES_SUMMARY
-- =====================================================
DROP TABLE IF EXISTS daily_sales_summary;

CREATE TABLE daily_sales_summary (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    restaurant_id           BIGINT NOT NULL,
    sales_date              DATE NOT NULL,
    total_orders            INT DEFAULT 0,
    dine_in_orders          INT DEFAULT 0,
    takeaway_orders         INT DEFAULT 0,
    delivery_orders         INT DEFAULT 0,
    cancelled_orders        INT DEFAULT 0,
    total_bills             INT DEFAULT 0,
    paid_bills              INT DEFAULT 0,
    pending_bills           INT DEFAULT 0,
    cancelled_bills         INT DEFAULT 0,
    total_revenue           DECIMAL(10,2) DEFAULT 0.00,
    subtotal_amount         DECIMAL(10,2) DEFAULT 0.00,
    discount_amount         DECIMAL(10,2) DEFAULT 0.00,
    service_charge_amount   DECIMAL(10,2) DEFAULT 0.00,
    packaging_charge_amount DECIMAL(10,2) DEFAULT 0.00,
    delivery_charge_amount  DECIMAL(10,2) DEFAULT 0.00,
    round_off_amount        DECIMAL(10,2) DEFAULT 0.00,
    pending_amount          DECIMAL(10,2) DEFAULT 0.00,
    cgst_amount             DECIMAL(10,2) DEFAULT 0.00,
    sgst_amount             DECIMAL(10,2) DEFAULT 0.00,
    igst_amount             DECIMAL(10,2) DEFAULT 0.00,
    total_tax_amount        DECIMAL(10,2) DEFAULT 0.00,
    cash_amount             DECIMAL(10,2) DEFAULT 0.00,
    card_amount             DECIMAL(10,2) DEFAULT 0.00,
    upi_amount              DECIMAL(10,2) DEFAULT 0.00,
    net_banking_amount      DECIMAL(10,2) DEFAULT 0.00,
    credit_amount           DECIMAL(10,2) DEFAULT 0.00,
    unique_customers        INT DEFAULT 0,
    new_customers           INT DEFAULT 0,
    average_bill_value      DECIMAL(10,2) DEFAULT 0.00,
    total_items_sold        INT DEFAULT 0,
    unique_items_sold       INT DEFAULT 0,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_daily_sales_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE CASCADE,
    UNIQUE KEY uk_daily_sales (restaurant_id, sales_date),
    INDEX idx_daily_sales_date (sales_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- RESTAURANT: add additional operational columns
-- =====================================================
CALL AddColumnUnlessExists('restaurant', 'state_code', "VARCHAR(2)");
CALL AddColumnUnlessExists('restaurant', 'fssai_license', "VARCHAR(20)");
CALL AddColumnUnlessExists('restaurant', 'service_charge_rate', "DECIMAL(5,2) DEFAULT 10.00");
CALL AddColumnUnlessExists('restaurant', 'packaging_charge', "DECIMAL(10,2) DEFAULT 0.00");
CALL AddColumnUnlessExists('restaurant', 'delivery_charge', "DECIMAL(10,2) DEFAULT 0.00");
CALL AddColumnUnlessExists('restaurant', 'min_delivery_amount', "DECIMAL(10,2) DEFAULT 0.00");

-- =====================================================
-- CLEANUP
-- =====================================================
DROP PROCEDURE IF EXISTS AddColumnUnlessExists;
SET FOREIGN_KEY_CHECKS = 1;