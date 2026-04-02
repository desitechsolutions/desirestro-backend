-- =====================================================
-- V7: Indian restaurant billing features
-- MySQL 8+ compatible
-- =====================================================
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- KOT: add order-level fields
-- =====================================================
ALTER TABLE kot
    ADD COLUMN IF NOT EXISTS order_type          VARCHAR(20) DEFAULT 'DINE_IN',
    ADD COLUMN IF NOT EXISTS customer_name       VARCHAR(100),
    ADD COLUMN IF NOT EXISTS customer_phone      VARCHAR(20),
    ADD COLUMN IF NOT EXISTS customer_address    TEXT,
    ADD COLUMN IF NOT EXISTS delivery_address    TEXT,
    ADD COLUMN IF NOT EXISTS special_instructions TEXT;

-- =====================================================
-- KOT_ITEMS: add item-level Indian features
-- =====================================================
ALTER TABLE kot_items
    ADD COLUMN IF NOT EXISTS spice_level          VARCHAR(20),
    ADD COLUMN IF NOT EXISTS special_instructions TEXT,
    ADD COLUMN IF NOT EXISTS is_jain              BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS hsn_code             VARCHAR(20);

-- =====================================================
-- MENU_ITEM: add Indian features
-- =====================================================
ALTER TABLE menu_item
    ADD COLUMN IF NOT EXISTS spice_level      VARCHAR(20) DEFAULT 'MEDIUM',
    ADD COLUMN IF NOT EXISTS is_jain          BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS is_swaminarayan  BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS hsn_code         VARCHAR(20),
    ADD COLUMN IF NOT EXISTS preparation_time INT DEFAULT 15;

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
    FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE CASCADE,
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

    -- Amount breakdown
    subtotal                DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    taxable_amount          DECIMAL(10,2) NOT NULL DEFAULT 0.00,

    -- Tax details (CGST_SGST | IGST | NO_TAX)
    tax_type                VARCHAR(20) DEFAULT 'CGST_SGST',
    cgst_rate               DECIMAL(5,2) DEFAULT 9.00,
    sgst_rate               DECIMAL(5,2) DEFAULT 9.00,
    igst_rate               DECIMAL(5,2) DEFAULT 0.00,
    cgst_amount             DECIMAL(10,2) DEFAULT 0.00,
    sgst_amount             DECIMAL(10,2) DEFAULT 0.00,
    igst_amount             DECIMAL(10,2) DEFAULT 0.00,
    total_tax               DECIMAL(10,2) DEFAULT 0.00,

    -- Additional charges
    service_charge_rate     DECIMAL(5,2) DEFAULT 10.00,
    service_charge_amount   DECIMAL(10,2) DEFAULT 0.00,
    packaging_charges       DECIMAL(10,2) DEFAULT 0.00,
    delivery_charges        DECIMAL(10,2) DEFAULT 0.00,

    -- Discounts
    discount_rate           DECIMAL(5,2) DEFAULT 0.00,
    discount_amount         DECIMAL(10,2) DEFAULT 0.00,
    discount_reason         VARCHAR(200),

    -- Final amounts
    total_before_round_off  DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    round_off_amount        DECIMAL(10,2) DEFAULT 0.00,
    grand_total             DECIMAL(10,2) NOT NULL DEFAULT 0.00,

    -- Payment
    payment_method          VARCHAR(20) DEFAULT 'CASH',
    payment_reference       VARCHAR(100),
    paid_amount             DECIMAL(10,2) DEFAULT 0.00,
    change_amount           DECIMAL(10,2) DEFAULT 0.00,

    -- Staff
    captain_id              BIGINT,
    cashier_id              BIGINT,

    -- Status
    is_paid                 BOOLEAN DEFAULT FALSE,
    is_cancelled            BOOLEAN DEFAULT FALSE,
    cancellation_reason     TEXT,

    -- Timestamps
    bill_time               DATETIME DEFAULT CURRENT_TIMESTAMP,
    payment_time            DATETIME NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id)   REFERENCES customer(id)   ON DELETE SET NULL,
    FOREIGN KEY (captain_id)    REFERENCES users(id)      ON DELETE SET NULL,
    FOREIGN KEY (cashier_id)    REFERENCES users(id)      ON DELETE SET NULL,
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
    FOREIGN KEY (bill_id) REFERENCES bill(id) ON DELETE CASCADE,
    INDEX idx_bill_item_bill (bill_id),
    INDEX idx_bill_item_menu (menu_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- DAILY_SALES_SUMMARY: pre-aggregated daily reporting
-- Column names match DailySalesSummary entity exactly
-- =====================================================
DROP TABLE IF EXISTS daily_sales_summary;

CREATE TABLE daily_sales_summary (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    restaurant_id           BIGINT NOT NULL,
    sales_date              DATE NOT NULL,

    -- Order statistics (by order type)
    total_orders            INT DEFAULT 0,
    dine_in_orders          INT DEFAULT 0,
    takeaway_orders         INT DEFAULT 0,
    delivery_orders         INT DEFAULT 0,
    cancelled_orders        INT DEFAULT 0,

    -- Bill counts
    total_bills             INT DEFAULT 0,
    paid_bills              INT DEFAULT 0,
    pending_bills           INT DEFAULT 0,
    cancelled_bills         INT DEFAULT 0,

    -- Revenue breakdown
    total_revenue           DECIMAL(10,2) DEFAULT 0.00,
    subtotal_amount         DECIMAL(10,2) DEFAULT 0.00,
    discount_amount         DECIMAL(10,2) DEFAULT 0.00,
    service_charge_amount   DECIMAL(10,2) DEFAULT 0.00,
    packaging_charge_amount DECIMAL(10,2) DEFAULT 0.00,
    delivery_charge_amount  DECIMAL(10,2) DEFAULT 0.00,
    round_off_amount        DECIMAL(10,2) DEFAULT 0.00,
    pending_amount          DECIMAL(10,2) DEFAULT 0.00,

    -- Tax breakdown
    cgst_amount             DECIMAL(10,2) DEFAULT 0.00,
    sgst_amount             DECIMAL(10,2) DEFAULT 0.00,
    igst_amount             DECIMAL(10,2) DEFAULT 0.00,
    total_tax_amount        DECIMAL(10,2) DEFAULT 0.00,

    -- Payment method breakdown
    cash_amount             DECIMAL(10,2) DEFAULT 0.00,
    card_amount             DECIMAL(10,2) DEFAULT 0.00,
    upi_amount              DECIMAL(10,2) DEFAULT 0.00,
    net_banking_amount      DECIMAL(10,2) DEFAULT 0.00,
    credit_amount           DECIMAL(10,2) DEFAULT 0.00,

    -- Customer metrics
    unique_customers        INT DEFAULT 0,
    new_customers           INT DEFAULT 0,
    average_bill_value      DECIMAL(10,2) DEFAULT 0.00,

    -- Item metrics
    total_items_sold        INT DEFAULT 0,
    unique_items_sold       INT DEFAULT 0,

    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE CASCADE,
    UNIQUE KEY uk_daily_sales (restaurant_id, sales_date),
    INDEX idx_daily_sales_date (sales_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- RESTAURANT: add additional operational columns
-- (gstin already exists from V4; skip to avoid duplicate)
-- =====================================================
ALTER TABLE restaurant
    ADD COLUMN IF NOT EXISTS state_code           VARCHAR(2),
    ADD COLUMN IF NOT EXISTS fssai_license        VARCHAR(20),
    ADD COLUMN IF NOT EXISTS service_charge_rate  DECIMAL(5,2) DEFAULT 10.00,
    ADD COLUMN IF NOT EXISTS packaging_charge     DECIMAL(10,2) DEFAULT 0.00,
    ADD COLUMN IF NOT EXISTS delivery_charge      DECIMAL(10,2) DEFAULT 0.00,
    ADD COLUMN IF NOT EXISTS min_delivery_amount  DECIMAL(10,2) DEFAULT 0.00;

SET FOREIGN_KEY_CHECKS = 1;
