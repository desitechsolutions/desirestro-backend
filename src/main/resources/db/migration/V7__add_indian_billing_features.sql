-- Migration for Indian restaurant billing features
-- Adds support for CGST/SGST/IGST, order types, spice levels, payment methods, etc.

-- Add new columns to menu_item table for Indian features
ALTER TABLE menu_item
ADD COLUMN spice_level VARCHAR(20) DEFAULT 'MEDIUM' COMMENT 'MILD, MEDIUM, HOT, EXTRA_HOT',
ADD COLUMN is_jain BOOLEAN DEFAULT FALSE COMMENT 'Jain food (no onion, garlic, root vegetables)',
ADD COLUMN is_swaminarayan BOOLEAN DEFAULT FALSE COMMENT 'Swaminarayan food restrictions',
ADD COLUMN hsn_code VARCHAR(20) COMMENT 'HSN code for GST',
ADD COLUMN preparation_time INT DEFAULT 15 COMMENT 'Preparation time in minutes';

-- Add new columns to orders table
ALTER TABLE orders
ADD COLUMN order_type VARCHAR(20) DEFAULT 'DINE_IN' COMMENT 'DINE_IN, TAKEAWAY, DELIVERY, PARCEL',
ADD COLUMN customer_name VARCHAR(100),
ADD COLUMN customer_phone VARCHAR(20),
ADD COLUMN customer_address TEXT,
ADD COLUMN delivery_address TEXT,
ADD COLUMN special_instructions TEXT;

-- Create customer table for regular customers
CREATE TABLE IF NOT EXISTS customer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    restaurant_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    gstin VARCHAR(15) COMMENT 'GST Identification Number',
    address TEXT,
    city VARCHAR(50),
    state VARCHAR(50),
    pincode VARCHAR(10),
    credit_limit DECIMAL(10,2) DEFAULT 0.00,
    credit_balance DECIMAL(10,2) DEFAULT 0.00,
    loyalty_points INT DEFAULT 0,
    total_orders INT DEFAULT 0,
    total_spent DECIMAL(10,2) DEFAULT 0.00,
    is_active BOOLEAN DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE CASCADE,
    UNIQUE KEY uk_customer_phone (restaurant_id, phone),
    INDEX idx_customer_phone (phone),
    INDEX idx_customer_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Enhanced bill table with Indian GST features
CREATE TABLE IF NOT EXISTS bill (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    restaurant_id BIGINT NOT NULL,
    bill_number VARCHAR(50) NOT NULL,
    order_id BIGINT NOT NULL,
    customer_id BIGINT,
    table_number INT,
    order_type VARCHAR(20) DEFAULT 'DINE_IN',
    
    -- Amount breakdown
    subtotal DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    taxable_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    
    -- Tax details
    tax_type VARCHAR(20) DEFAULT 'CGST_SGST' COMMENT 'CGST_SGST, IGST, NO_TAX',
    cgst_rate DECIMAL(5,2) DEFAULT 9.00,
    sgst_rate DECIMAL(5,2) DEFAULT 9.00,
    igst_rate DECIMAL(5,2) DEFAULT 0.00,
    cgst_amount DECIMAL(10,2) DEFAULT 0.00,
    sgst_amount DECIMAL(10,2) DEFAULT 0.00,
    igst_amount DECIMAL(10,2) DEFAULT 0.00,
    total_tax DECIMAL(10,2) DEFAULT 0.00,
    
    -- Additional charges
    service_charge_rate DECIMAL(5,2) DEFAULT 10.00,
    service_charge_amount DECIMAL(10,2) DEFAULT 0.00,
    packaging_charges DECIMAL(10,2) DEFAULT 0.00,
    delivery_charges DECIMAL(10,2) DEFAULT 0.00,
    
    -- Discounts
    discount_rate DECIMAL(5,2) DEFAULT 0.00,
    discount_amount DECIMAL(10,2) DEFAULT 0.00,
    discount_reason VARCHAR(200),
    
    -- Final amounts
    total_before_round_off DECIMAL(10,2) NOT NULL,
    round_off_amount DECIMAL(10,2) DEFAULT 0.00,
    grand_total DECIMAL(10,2) NOT NULL,
    
    -- Payment details
    payment_method VARCHAR(20) DEFAULT 'CASH' COMMENT 'CASH, UPI, CARD, WALLET, CREDIT_ACCOUNT, ONLINE',
    payment_reference VARCHAR(100) COMMENT 'UPI transaction ID, card last 4 digits, etc.',
    paid_amount DECIMAL(10,2) DEFAULT 0.00,
    change_amount DECIMAL(10,2) DEFAULT 0.00,
    
    -- Staff details
    captain_id BIGINT,
    cashier_id BIGINT,
    
    -- Status
    is_paid BOOLEAN DEFAULT FALSE,
    is_cancelled BOOLEAN DEFAULT FALSE,
    cancellation_reason TEXT,
    
    -- Timestamps
    bill_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    payment_time TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE CASCADE,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE SET NULL,
    FOREIGN KEY (captain_id) REFERENCES user(id) ON DELETE SET NULL,
    FOREIGN KEY (cashier_id) REFERENCES user(id) ON DELETE SET NULL,
    UNIQUE KEY uk_bill_number (restaurant_id, bill_number),
    INDEX idx_bill_date (bill_time),
    INDEX idx_bill_payment (payment_method, is_paid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bill items table
CREATE TABLE IF NOT EXISTS bill_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    menu_item_id BIGINT NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    item_code VARCHAR(50),
    category VARCHAR(50),
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    item_total DECIMAL(10,2) NOT NULL,
    spice_level VARCHAR(20),
    special_instructions TEXT,
    is_veg BOOLEAN DEFAULT TRUE,
    is_jain BOOLEAN DEFAULT FALSE,
    hsn_code VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (bill_id) REFERENCES bill(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_item_id) REFERENCES menu_item(id) ON DELETE RESTRICT,
    INDEX idx_bill_item (bill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Daily sales summary table for reporting
CREATE TABLE IF NOT EXISTS daily_sales_summary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    restaurant_id BIGINT NOT NULL,
    sale_date DATE NOT NULL,
    
    -- Order statistics
    total_orders INT DEFAULT 0,
    dine_in_orders INT DEFAULT 0,
    takeaway_orders INT DEFAULT 0,
    delivery_orders INT DEFAULT 0,
    cancelled_orders INT DEFAULT 0,
    
    -- Revenue breakdown
    gross_sales DECIMAL(10,2) DEFAULT 0.00,
    total_discount DECIMAL(10,2) DEFAULT 0.00,
    net_sales DECIMAL(10,2) DEFAULT 0.00,
    
    -- Tax breakdown
    total_cgst DECIMAL(10,2) DEFAULT 0.00,
    total_sgst DECIMAL(10,2) DEFAULT 0.00,
    total_igst DECIMAL(10,2) DEFAULT 0.00,
    total_tax DECIMAL(10,2) DEFAULT 0.00,
    
    -- Additional charges
    total_service_charge DECIMAL(10,2) DEFAULT 0.00,
    total_packaging_charges DECIMAL(10,2) DEFAULT 0.00,
    total_delivery_charges DECIMAL(10,2) DEFAULT 0.00,
    
    -- Payment method breakdown
    cash_sales DECIMAL(10,2) DEFAULT 0.00,
    upi_sales DECIMAL(10,2) DEFAULT 0.00,
    card_sales DECIMAL(10,2) DEFAULT 0.00,
    wallet_sales DECIMAL(10,2) DEFAULT 0.00,
    credit_sales DECIMAL(10,2) DEFAULT 0.00,
    online_sales DECIMAL(10,2) DEFAULT 0.00,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (restaurant_id) REFERENCES restaurant(id) ON DELETE CASCADE,
    UNIQUE KEY uk_daily_sales (restaurant_id, sale_date),
    INDEX idx_sale_date (sale_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add restaurant GST details
ALTER TABLE restaurant
ADD COLUMN gstin VARCHAR(15) COMMENT 'GST Identification Number',
ADD COLUMN state_code VARCHAR(2) COMMENT 'State code for GST (e.g., 29 for Karnataka)',
ADD COLUMN fssai_license VARCHAR(20) COMMENT 'FSSAI License Number',
ADD COLUMN service_charge_rate DECIMAL(5,2) DEFAULT 10.00,
ADD COLUMN packaging_charge DECIMAL(10,2) DEFAULT 0.00,
ADD COLUMN delivery_charge DECIMAL(10,2) DEFAULT 0.00,
ADD COLUMN min_delivery_amount DECIMAL(10,2) DEFAULT 0.00;

-- Create indexes for better performance
CREATE INDEX idx_menu_item_spice ON menu_item(spice_level);
CREATE INDEX idx_menu_item_dietary ON menu_item(is_jain, is_swaminarayan);
CREATE INDEX idx_order_type ON orders(order_type);
CREATE INDEX idx_customer_active ON customer(is_active);
CREATE INDEX idx_bill_status ON bill(is_paid, is_cancelled);

-- Insert sample data for testing (optional)
-- You can uncomment these if you want sample data

-- INSERT INTO customer (restaurant_id, name, phone, email, address, city, state, pincode)
-- VALUES 
-- (1, 'Rajesh Kumar', '9876543210', 'rajesh@example.com', '123 MG Road', 'Bangalore', 'Karnataka', '560001'),
-- (1, 'Priya Sharma', '9876543211', 'priya@example.com', '456 Brigade Road', 'Bangalore', 'Karnataka', '560002');

COMMIT;

-- Made with Bob
