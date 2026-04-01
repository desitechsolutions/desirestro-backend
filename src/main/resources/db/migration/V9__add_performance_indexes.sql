-- Migration V9: Add Performance Indexes
-- This migration adds indexes to improve query performance across the application

-- ============================================
-- CUSTOMER TABLE INDEXES
-- ============================================

-- Index for searching customers by phone within a restaurant
CREATE INDEX idx_customer_restaurant_phone ON customer(restaurant_id, phone);

-- Index for searching customers by email within a restaurant
CREATE INDEX idx_customer_restaurant_email ON customer(restaurant_id, email);

-- Index for searching customers by GSTIN within a restaurant
CREATE INDEX idx_customer_restaurant_gstin ON customer(restaurant_id, gstin);

-- Index for filtering active customers
CREATE INDEX idx_customer_restaurant_active ON customer(restaurant_id, is_active);

-- Index for customer credit queries
CREATE INDEX idx_customer_credit_balance ON customer(restaurant_id, credit_balance);

-- Index for loyalty points queries
CREATE INDEX idx_customer_loyalty_points ON customer(restaurant_id, loyalty_points);

-- ============================================
-- BILL TABLE INDEXES
-- ============================================

-- Composite index for date range queries (most common query pattern)
CREATE INDEX idx_bill_restaurant_date ON bill(restaurant_id, bill_date);

-- Index for customer bill history
CREATE INDEX idx_bill_restaurant_customer ON bill(restaurant_id, customer_id);

-- Index for payment status queries (unpaid bills, etc.)
CREATE INDEX idx_bill_restaurant_status ON bill(restaurant_id, payment_status);

-- Index for payment method analytics
CREATE INDEX idx_bill_restaurant_payment_method ON bill(restaurant_id, payment_method);

-- Index for tax type queries
CREATE INDEX idx_bill_restaurant_tax_type ON bill(restaurant_id, tax_type);

-- Index for date range queries without restaurant filter (for super admin)
CREATE INDEX idx_bill_date_range ON bill(bill_date);

-- Index for KOT to bill lookup
CREATE INDEX idx_bill_kot ON bill(kot_id);

-- ============================================
-- BILL_ITEM TABLE INDEXES
-- ============================================

-- Index for bill items lookup (most common query)
CREATE INDEX idx_bill_item_bill ON bill_item(bill_id);

-- Index for menu item sales analytics
CREATE INDEX idx_bill_item_menu ON bill_item(menu_item_id);

-- Composite index for restaurant-level item analytics
CREATE INDEX idx_bill_item_restaurant_menu ON bill_item(restaurant_id, menu_item_id);

-- ============================================
-- DAILY_SALES_SUMMARY TABLE INDEXES
-- ============================================

-- Composite index for daily summary queries (primary query pattern)
CREATE INDEX idx_daily_sales_restaurant_date ON daily_sales_summary(restaurant_id, sale_date);

-- Index for date range queries
CREATE INDEX idx_daily_sales_date_range ON daily_sales_summary(sale_date);

-- ============================================
-- KOT TABLE INDEXES
-- ============================================

-- Index for restaurant KOT queries
CREATE INDEX idx_kot_restaurant_status ON kot(restaurant_id, status);

-- Index for party KOT lookup
CREATE INDEX idx_kot_party ON kot(party_id);

-- Index for KOT number lookup
CREATE INDEX idx_kot_restaurant_number ON kot(restaurant_id, kot_number);

-- ============================================
-- MENU_ITEM TABLE INDEXES
-- ============================================

-- Index for available menu items
CREATE INDEX idx_menu_item_restaurant_available ON menu_item(restaurant_id, available);

-- Index for category filtering
CREATE INDEX idx_menu_item_restaurant_category ON menu_item(restaurant_id, category_id);

-- Index for veg/non-veg filtering
CREATE INDEX idx_menu_item_restaurant_veg ON menu_item(restaurant_id, veg);

-- ============================================
-- PARTY TABLE INDEXES
-- ============================================

-- Index for table party lookup
CREATE INDEX idx_party_restaurant_table ON party(restaurant_id, table_number);

-- Index for active parties
CREATE INDEX idx_party_restaurant_active ON party(restaurant_id, is_active);

-- ============================================
-- RESTAURANT_TABLE INDEXES
-- ============================================

-- Index for available tables
CREATE INDEX idx_table_restaurant_status ON restaurant_table(restaurant_id, status);

-- ============================================
-- STAFF TABLE INDEXES (if exists)
-- ============================================

-- Index for active staff
CREATE INDEX idx_staff_restaurant_active ON staff(restaurant_id, is_active);

-- Index for staff role queries
CREATE INDEX idx_staff_restaurant_role ON staff(restaurant_id, role);

-- ============================================
-- AUDIT_LOG TABLE INDEXES
-- ============================================

-- Index for restaurant audit logs
CREATE INDEX idx_audit_restaurant_date ON audit_log(restaurant_id, created_at);

-- Index for entity audit trail
CREATE INDEX idx_audit_entity ON audit_log(entity_type, entity_id);

-- Index for user action audit
CREATE INDEX idx_audit_user ON audit_log(user_id, created_at);

-- Index for action type filtering
CREATE INDEX idx_audit_action ON audit_log(action, created_at);

-- ============================================
-- PERFORMANCE STATISTICS
-- ============================================

-- Analyze tables to update statistics for query optimizer
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

-- Add comments for documentation
ALTER TABLE customer COMMENT = 'Customer master with indexes for fast search and filtering';
ALTER TABLE bill COMMENT = 'Bill transactions with indexes optimized for date range and analytics queries';
ALTER TABLE bill_item COMMENT = 'Bill line items with indexes for sales analytics';
ALTER TABLE daily_sales_summary COMMENT = 'Pre-aggregated daily sales data with date-based indexes';

-- Made with Bob
