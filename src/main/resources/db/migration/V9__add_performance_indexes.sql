-- =====================================================
-- V9: Performance indexes
-- MySQL 8+ (uses CREATE INDEX IF NOT EXISTS)
-- =====================================================

-- ============================================
-- CUSTOMER
-- ============================================
CREATE INDEX IF NOT EXISTS idx_customer_restaurant_phone   ON customer(restaurant_id, phone);
CREATE INDEX IF NOT EXISTS idx_customer_restaurant_email   ON customer(restaurant_id, email);
CREATE INDEX IF NOT EXISTS idx_customer_restaurant_gstin   ON customer(restaurant_id, gstin);
CREATE INDEX IF NOT EXISTS idx_customer_restaurant_active  ON customer(restaurant_id, is_active);
CREATE INDEX IF NOT EXISTS idx_customer_credit_balance     ON customer(restaurant_id, credit_balance);
CREATE INDEX IF NOT EXISTS idx_customer_loyalty_points     ON customer(restaurant_id, loyalty_points);

-- ============================================
-- BILL  (bill_time is the timestamp column; bill has no payment_status or kot_id)
-- ============================================
CREATE INDEX IF NOT EXISTS idx_bill_restaurant_time        ON bill(restaurant_id, bill_time);
CREATE INDEX IF NOT EXISTS idx_bill_restaurant_customer    ON bill(restaurant_id, customer_id);
CREATE INDEX IF NOT EXISTS idx_bill_restaurant_paid        ON bill(restaurant_id, is_paid);
CREATE INDEX IF NOT EXISTS idx_bill_restaurant_pay_method  ON bill(restaurant_id, payment_method);
CREATE INDEX IF NOT EXISTS idx_bill_restaurant_tax_type    ON bill(restaurant_id, tax_type);
CREATE INDEX IF NOT EXISTS idx_bill_date_range             ON bill(bill_time);

-- ============================================
-- BILL_ITEM  (bill_item has no restaurant_id column)
-- ============================================
CREATE INDEX IF NOT EXISTS idx_bill_item_bill  ON bill_item(bill_id);
CREATE INDEX IF NOT EXISTS idx_bill_item_menu  ON bill_item(menu_item_id);

-- ============================================
-- DAILY_SALES_SUMMARY  (column is sales_date not sale_date)
-- ============================================
CREATE INDEX IF NOT EXISTS idx_daily_sales_restaurant_date ON daily_sales_summary(restaurant_id, sales_date);
CREATE INDEX IF NOT EXISTS idx_daily_sales_date_range      ON daily_sales_summary(sales_date);

-- ============================================
-- KOT
-- ============================================
CREATE INDEX IF NOT EXISTS idx_kot_restaurant_status  ON kot(restaurant_id, status);
CREATE INDEX IF NOT EXISTS idx_kot_party              ON kot(party_id);
CREATE INDEX IF NOT EXISTS idx_kot_restaurant_number  ON kot(restaurant_id, kot_number);

-- ============================================
-- MENU_ITEM
-- ============================================
CREATE INDEX IF NOT EXISTS idx_menu_item_restaurant_available ON menu_item(restaurant_id, available);
CREATE INDEX IF NOT EXISTS idx_menu_item_restaurant_category  ON menu_item(restaurant_id, category_id);
CREATE INDEX IF NOT EXISTS idx_menu_item_restaurant_veg       ON menu_item(restaurant_id, veg);

-- ============================================
-- PARTY  (FK column is table_id; no is_active — use status)
-- ============================================
CREATE INDEX IF NOT EXISTS idx_party_restaurant_table  ON party(restaurant_id, table_id);
CREATE INDEX IF NOT EXISTS idx_party_restaurant_status ON party(restaurant_id, status);

-- ============================================
-- RESTAURANT_TABLE
-- ============================================
CREATE INDEX IF NOT EXISTS idx_table_restaurant_status ON restaurant_table(restaurant_id, status);

-- ============================================
-- STAFF  (V2 dropped username/password/role from staff; no is_active column)
-- ============================================
CREATE INDEX IF NOT EXISTS idx_staff_restaurant_join_date ON staff(restaurant_id, join_date);

-- ============================================
-- AUDIT_LOG  (timestamp column; V6 CREATE TABLE already adds basic indexes)
-- ============================================
CREATE INDEX IF NOT EXISTS idx_audit_restaurant_ts  ON audit_log(restaurant_id, timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_entity         ON audit_log(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_user_ts        ON audit_log(user_id, timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_action_ts      ON audit_log(action, timestamp);

-- ============================================
-- ANALYZE for query optimizer statistics
-- ============================================
ANALYZE TABLE customer;
ANALYZE TABLE bill;
ANALYZE TABLE bill_item;
ANALYZE TABLE daily_sales_summary;
ANALYZE TABLE kot;
ANALYZE TABLE menu_item;
ANALYZE TABLE party;
ANALYZE TABLE restaurant_table;
ANALYZE TABLE staff;
ANALYZE TABLE audit_log;

-- Table comments
ALTER TABLE customer           COMMENT = 'Customer master with indexes for fast search and filtering';
ALTER TABLE bill               COMMENT = 'Bill transactions optimised for date-range and analytics queries';
ALTER TABLE bill_item          COMMENT = 'Bill line items with indexes for sales analytics';
ALTER TABLE daily_sales_summary COMMENT = 'Pre-aggregated daily sales data with date-based indexes';
