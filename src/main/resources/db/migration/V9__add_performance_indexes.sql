-- V9: Performance indexes for DesiRestro
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- HELPER PROCEDURE: AddIndexUnlessExists
-- =====================================================
DROP PROCEDURE IF EXISTS AddIndexUnlessExists;
DELIMITER //
CREATE PROCEDURE AddIndexUnlessExists(
    IN tableName VARCHAR(64),
    IN indexName VARCHAR(64),
    IN indexDefinition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.statistics
        WHERE table_schema = DATABASE()
        AND table_name = tableName
        AND index_name = indexName
    ) THEN
        SET @sql = CONCAT('CREATE INDEX ', indexName, ' ON ', tableName, ' ', indexDefinition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

-- ============================================
-- CUSTOMER
-- ============================================
CALL AddIndexUnlessExists('customer', 'idx_customer_restaurant_phone', '(restaurant_id, phone)');
CALL AddIndexUnlessExists('customer', 'idx_customer_restaurant_email', '(restaurant_id, email)');
CALL AddIndexUnlessExists('customer', 'idx_customer_restaurant_gstin', '(restaurant_id, gstin)');
CALL AddIndexUnlessExists('customer', 'idx_customer_restaurant_active', '(restaurant_id, is_active)');
CALL AddIndexUnlessExists('customer', 'idx_customer_credit_balance', '(restaurant_id, credit_balance)');
CALL AddIndexUnlessExists('customer', 'idx_customer_loyalty_points', '(restaurant_id, loyalty_points)');

-- ============================================
-- BILL
-- ============================================
CALL AddIndexUnlessExists('bill', 'idx_bill_restaurant_time', '(restaurant_id, bill_time)');
CALL AddIndexUnlessExists('bill', 'idx_bill_restaurant_customer', '(restaurant_id, customer_id)');
CALL AddIndexUnlessExists('bill', 'idx_bill_restaurant_paid', '(restaurant_id, is_paid)');
CALL AddIndexUnlessExists('bill', 'idx_bill_restaurant_pay_method', '(restaurant_id, payment_method)');
CALL AddIndexUnlessExists('bill', 'idx_bill_restaurant_tax_type', '(restaurant_id, tax_type)');
CALL AddIndexUnlessExists('bill', 'idx_bill_date_range', '(bill_time)');

-- ============================================
-- BILL_ITEM
-- ============================================
CALL AddIndexUnlessExists('bill_item', 'idx_bill_item_bill', '(bill_id)');
CALL AddIndexUnlessExists('bill_item', 'idx_bill_item_menu', '(menu_item_id)');

-- ============================================
-- DAILY_SALES_SUMMARY
-- ============================================
CALL AddIndexUnlessExists('daily_sales_summary', 'idx_daily_sales_restaurant_date', '(restaurant_id, sales_date)');
CALL AddIndexUnlessExists('daily_sales_summary', 'idx_daily_sales_date_range', '(sales_date)');

-- ============================================
-- KOT
-- ============================================
CALL AddIndexUnlessExists('kot', 'idx_kot_restaurant_status', '(restaurant_id, status)');
CALL AddIndexUnlessExists('kot', 'idx_kot_party', '(party_id)');
CALL AddIndexUnlessExists('kot', 'idx_kot_restaurant_number', '(restaurant_id, kot_number)');

-- ============================================
-- MENU_ITEM
-- ============================================
CALL AddIndexUnlessExists('menu_item', 'idx_menu_item_restaurant_available', '(restaurant_id, available)');
CALL AddIndexUnlessExists('menu_item', 'idx_menu_item_restaurant_category', '(restaurant_id, category_id)');
CALL AddIndexUnlessExists('menu_item', 'idx_menu_item_restaurant_veg', '(restaurant_id, veg)');

-- ============================================
-- PARTY
-- ============================================
CALL AddIndexUnlessExists('party', 'idx_party_restaurant_table', '(restaurant_id, table_id)');
CALL AddIndexUnlessExists('party', 'idx_party_restaurant_status', '(restaurant_id, status)');

-- ============================================
-- RESTAURANT_TABLE
-- ============================================
CALL AddIndexUnlessExists('restaurant_table', 'idx_table_restaurant_status', '(restaurant_id, status)');

-- ============================================
-- STAFF
-- ============================================
CALL AddIndexUnlessExists('staff', 'idx_staff_restaurant_join_date', '(restaurant_id, join_date)');

-- ============================================
-- AUDIT_LOG
-- ============================================
CALL AddIndexUnlessExists('audit_log', 'idx_audit_restaurant_ts', '(restaurant_id, timestamp)');
CALL AddIndexUnlessExists('audit_log', 'idx_audit_entity_new', '(entity_type, entity_id)');
CALL AddIndexUnlessExists('audit_log', 'idx_audit_user_ts', '(user_id, timestamp)');
CALL AddIndexUnlessExists('audit_log', 'idx_audit_action_ts', '(action, timestamp)');

-- ============================================
-- ANALYZE & COMMENTS
-- ============================================
ANALYZE TABLE customer, bill, bill_item, daily_sales_summary, kot, menu_item, party, restaurant_table, staff, audit_log;

ALTER TABLE customer           COMMENT = 'Customer master with indexes for fast search and filtering';
ALTER TABLE bill               COMMENT = 'Bill transactions optimised for date-range and analytics queries';
ALTER TABLE bill_item          COMMENT = 'Bill line items with indexes for sales analytics';
ALTER TABLE daily_sales_summary COMMENT = 'Pre-aggregated daily sales data with date-based indexes';

-- ============================================
-- CLEANUP
-- ============================================
DROP PROCEDURE IF EXISTS AddIndexUnlessExists;
SET FOREIGN_KEY_CHECKS = 1;