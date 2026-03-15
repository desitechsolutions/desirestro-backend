-- V3__seed_initial_data.sql
-- Seed initial data for Indian restaurant application

SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- ADMIN USER (password: admin@123 BCrypt encoded)
-- =====================================================
INSERT IGNORE INTO users (username, password, role, full_name) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LpMpPiMSze2', 'ADMIN', 'Restaurant Admin'),
('captain1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LpMpPiMSze2', 'CAPTAIN', 'Captain Raj'),
('kitchen1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LpMpPiMSze2', 'KITCHEN', 'Kitchen Chef'),
('cashier1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LpMpPiMSze2', 'CASHIER', 'Cashier Ram');

-- =====================================================
-- RESTAURANT TABLES
-- =====================================================
INSERT IGNORE INTO restaurant_table (table_number, capacity, occupied_seats, status) VALUES
('T1', 4, 0, 'EMPTY'),
('T2', 6, 0, 'EMPTY'),
('T3', 4, 0, 'EMPTY'),
('T4', 8, 0, 'EMPTY'),
('T5', 2, 0, 'EMPTY'),
('T6', 4, 0, 'EMPTY'),
('T7', 6, 0, 'EMPTY'),
('T8', 4, 0, 'EMPTY'),
('T9', 10, 0, 'EMPTY'),
('T10', 2, 0, 'EMPTY');

-- =====================================================
-- MENU CATEGORIES
-- =====================================================
INSERT IGNORE INTO category (name, display_order) VALUES
('Starters', 1),
('Soups', 2),
('Breads', 3),
('Main Course - Veg', 4),
('Main Course - Non Veg', 5),
('Rice & Biryani', 6),
('Desserts', 7),
('Beverages', 8);

-- =====================================================
-- MENU ITEMS (Starters - category_id=1)
-- =====================================================
INSERT IGNORE INTO menu_item (name, description, price, veg, available, category_id) VALUES
-- Starters (id=1)
('Paneer Tikka', 'Cottage cheese marinated in spices and grilled in tandoor', 249.00, TRUE, TRUE, 1),
('Veg Seekh Kabab', 'Mixed vegetable kebab with aromatic spices', 199.00, TRUE, TRUE, 1),
('Hara Bhara Kabab', 'Spinach and paneer kebab with fresh herbs', 179.00, TRUE, TRUE, 1),
('Chicken Tikka', 'Tender chicken marinated in yogurt and spices, tandoor grilled', 329.00, FALSE, TRUE, 1),
('Chicken 65', 'South Indian style spicy deep fried chicken', 299.00, FALSE, TRUE, 1),
('Mutton Seekh Kabab', 'Minced mutton kebab with onion and spices', 379.00, FALSE, TRUE, 1),
('Fish Tikka', 'Fresh fish marinated in tandoori spices', 349.00, FALSE, TRUE, 1),
-- Soups (id=2)
('Tomato Soup', 'Classic Indian tomato soup with cream', 129.00, TRUE, TRUE, 2),
('Sweet Corn Soup', 'Chinese-style corn soup with vegetables', 149.00, TRUE, TRUE, 2),
('Chicken Hot & Sour Soup', 'Spicy tangy chicken soup with vegetables', 179.00, FALSE, TRUE, 2),
-- Breads (id=3)
('Tandoori Roti', 'Whole wheat bread baked in tandoor', 45.00, TRUE, TRUE, 3),
('Butter Naan', 'Soft leavened bread with butter, baked in tandoor', 60.00, TRUE, TRUE, 3),
('Garlic Naan', 'Naan topped with garlic and butter', 70.00, TRUE, TRUE, 3),
('Stuffed Kulcha', 'Naan stuffed with spiced potato filling', 90.00, TRUE, TRUE, 3),
('Paratha', 'Layered flatbread cooked on griddle', 55.00, TRUE, TRUE, 3),
-- Main Course Veg (id=4)
('Paneer Butter Masala', 'Cottage cheese in rich tomato-cream gravy', 299.00, TRUE, TRUE, 4),
('Dal Makhani', 'Black lentils slow-cooked with butter and cream', 249.00, TRUE, TRUE, 4),
('Palak Paneer', 'Cottage cheese in spinach gravy', 279.00, TRUE, TRUE, 4),
('Shahi Paneer', 'Paneer in cashew-cream royal gravy', 319.00, TRUE, TRUE, 4),
('Chole Bhature', 'Spicy chickpea curry with fried puffed bread', 229.00, TRUE, TRUE, 4),
('Mix Veg Curry', 'Seasonal vegetables in spiced curry', 219.00, TRUE, TRUE, 4),
('Aloo Gobi', 'Potato and cauliflower dry curry', 199.00, TRUE, TRUE, 4),
-- Main Course Non Veg (id=5)
('Butter Chicken', 'Tender chicken in creamy tomato-butter sauce', 429.00, FALSE, TRUE, 5),
('Chicken Kadai', 'Chicken cooked with bell peppers in kadai masala', 399.00, FALSE, TRUE, 5),
('Mutton Rogan Josh', 'Slow-cooked mutton in Kashmiri spices', 499.00, FALSE, TRUE, 5),
('Mutton Korma', 'Mutton in rich yogurt-cream gravy', 479.00, FALSE, TRUE, 5),
('Prawn Masala', 'Fresh prawns in spiced onion-tomato gravy', 449.00, FALSE, TRUE, 5),
-- Rice & Biryani (id=6)
('Veg Biryani', 'Fragrant basmati rice with seasonal vegetables', 299.00, TRUE, TRUE, 6),
('Paneer Biryani', 'Aromatic rice with spiced cottage cheese', 349.00, TRUE, TRUE, 6),
('Chicken Biryani', 'Classic Hyderabadi dum chicken biryani', 399.00, FALSE, TRUE, 6),
('Mutton Biryani', 'Slow-cooked mutton with fragrant basmati rice', 479.00, FALSE, TRUE, 6),
('Steamed Rice', 'Plain steamed basmati rice', 99.00, TRUE, TRUE, 6),
-- Desserts (id=7)
('Gulab Jamun', 'Soft milk-solid balls soaked in rose-flavored sugar syrup', 149.00, TRUE, TRUE, 7),
('Rasmalai', 'Soft cheese patties soaked in cardamom-flavored milk', 179.00, TRUE, TRUE, 7),
('Kheer', 'Rice pudding with nuts and saffron', 149.00, TRUE, TRUE, 7),
('Kulfi', 'Traditional Indian ice cream with pistachio', 129.00, TRUE, TRUE, 7),
('Jalebi', 'Crispy deep-fried spirals soaked in sugar syrup', 119.00, TRUE, TRUE, 7),
-- Beverages (id=8)
('Masala Chai', 'Spiced Indian tea with ginger and cardamom', 69.00, TRUE, TRUE, 8),
('Filter Coffee', 'Strong South Indian drip coffee with milk', 79.00, TRUE, TRUE, 8),
('Sweet Lassi', 'Chilled sweet yogurt drink', 99.00, TRUE, TRUE, 8),
('Mango Lassi', 'Chilled mango yogurt drink', 129.00, TRUE, TRUE, 8),
('Fresh Lime Soda', 'Lime with soda, sweet or salty', 79.00, TRUE, TRUE, 8),
('Butter Milk (Chaach)', 'Spiced chilled buttermilk', 69.00, TRUE, TRUE, 8);

-- =====================================================
-- INGREDIENTS (common for Indian restaurant)
-- =====================================================
INSERT IGNORE INTO ingredient (name, unit, current_stock, reorder_level) VALUES
('Chicken', 'kg', 50.0, 10.0),
('Mutton', 'kg', 30.0, 8.0),
('Fish', 'kg', 20.0, 5.0),
('Prawns', 'kg', 15.0, 4.0),
('Paneer', 'kg', 25.0, 6.0),
('Basmati Rice', 'kg', 100.0, 20.0),
('Whole Wheat Flour (Atta)', 'kg', 80.0, 15.0),
('Maida (All Purpose Flour)', 'kg', 50.0, 10.0),
('Onion', 'kg', 60.0, 15.0),
('Tomato', 'kg', 50.0, 12.0),
('Ginger', 'kg', 10.0, 2.0),
('Garlic', 'kg', 10.0, 2.0),
('Cooking Oil', 'litre', 40.0, 10.0),
('Butter', 'kg', 15.0, 4.0),
('Fresh Cream', 'litre', 10.0, 3.0),
('Milk', 'litre', 30.0, 8.0),
('Curd (Yogurt)', 'kg', 20.0, 5.0),
('Chickpeas', 'kg', 20.0, 5.0),
('Spinach', 'kg', 15.0, 4.0),
('Mixed Vegetables', 'kg', 30.0, 8.0),
('Potato', 'kg', 40.0, 10.0),
('Cauliflower', 'kg', 20.0, 5.0),
('Bell Pepper', 'kg', 15.0, 4.0),
('Mango Pulp', 'litre', 10.0, 3.0),
('Sugar', 'kg', 30.0, 8.0),
('Cashews', 'kg', 5.0, 1.0),
('Saffron', 'g', 100.0, 20.0),
('Cardamom', 'g', 500.0, 100.0),
('Garam Masala', 'g', 1000.0, 200.0),
('Tandoori Masala', 'g', 1000.0, 200.0);

SET FOREIGN_KEY_CHECKS = 1;
