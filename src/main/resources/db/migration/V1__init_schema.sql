-- =====================================================
-- DATABASE SETTINGS
-- =====================================================
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- USERS (Spring Security)
-- =====================================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    full_name VARCHAR(150),
    UNIQUE KEY uk_users_username (username),
    INDEX idx_users_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- STAFF
-- =====================================================
CREATE TABLE staff (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(150),
    join_date DATE,
    UNIQUE KEY uk_staff_username (username),
    INDEX idx_staff_role (role),
    INDEX idx_staff_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- ATTENDANCE
-- =====================================================
CREATE TABLE attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    staff_id BIGINT NOT NULL,
    date DATE NOT NULL,
    clock_in DATETIME,
    clock_out DATETIME,
    INDEX idx_attendance_staff_date (staff_id, date),
    CONSTRAINT fk_attendance_staff
        FOREIGN KEY (staff_id) REFERENCES staff(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- LEAVE REQUEST
-- =====================================================
CREATE TABLE leave_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    staff_id BIGINT NOT NULL,
    from_date DATE NOT NULL,
    to_date DATE NOT NULL,
    reason VARCHAR(255),
    status VARCHAR(50),
    applied_date DATE,
    approved_date DATE,
    INDEX idx_leave_staff (staff_id),
    INDEX idx_leave_status (status),
    CONSTRAINT fk_leave_staff
        FOREIGN KEY (staff_id) REFERENCES staff(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- INGREDIENT
-- =====================================================
CREATE TABLE ingredient (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    unit VARCHAR(20),
    current_stock DOUBLE DEFAULT 0,
    reorder_level DOUBLE DEFAULT 0,
    UNIQUE KEY uk_ingredient_name (name),
    INDEX idx_ingredient_stock (current_stock)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- CATEGORY
-- =====================================================
CREATE TABLE category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    display_order INT,
    UNIQUE KEY uk_category_name (name),
    INDEX idx_category_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- MENU ITEM
-- =====================================================
CREATE TABLE menu_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(255),
    price DOUBLE NOT NULL,
    veg BOOLEAN DEFAULT TRUE,
    available BOOLEAN DEFAULT TRUE,
    category_id BIGINT,
    INDEX idx_menu_category (category_id),
    INDEX idx_menu_available (available),
    INDEX idx_menu_veg (veg),
    CONSTRAINT fk_menu_category
        FOREIGN KEY (category_id) REFERENCES category(id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- MENU ITEM INGREDIENT (MANY-TO-MANY)
-- =====================================================
CREATE TABLE menu_item_ingredient (
    menu_item_id BIGINT NOT NULL,
    ingredient_id BIGINT NOT NULL,
    quantity_required DOUBLE NOT NULL,
    PRIMARY KEY (menu_item_id, ingredient_id),
    INDEX idx_mii_ingredient (ingredient_id),
    CONSTRAINT fk_mii_menu_item
        FOREIGN KEY (menu_item_id) REFERENCES menu_item(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_mii_ingredient
        FOREIGN KEY (ingredient_id) REFERENCES ingredient(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- RESTAURANT TABLE
-- =====================================================
CREATE TABLE restaurant_table (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_number VARCHAR(20) NOT NULL,
    capacity INT NOT NULL,
    occupied_seats INT DEFAULT 0,
    status VARCHAR(50) DEFAULT 'EMPTY',
    current_captain VARCHAR(100),
    UNIQUE KEY uk_table_number (table_number),
    INDEX idx_table_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- PARTY
-- =====================================================
CREATE TABLE party (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_id BIGINT NOT NULL,
    occupied_seats INT,
    arrived_at DATETIME,
    status VARCHAR(50),
    INDEX idx_party_table (table_id),
    INDEX idx_party_status (status),
    CONSTRAINT fk_party_table
        FOREIGN KEY (table_id) REFERENCES restaurant_table(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- KOT (Kitchen Order Ticket)
-- =====================================================
CREATE TABLE kot (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kot_number VARCHAR(50) NOT NULL,
    party_id BIGINT NOT NULL,
    created_at DATETIME,
    status VARCHAR(50),
    UNIQUE KEY uk_kot_number (kot_number),
    INDEX idx_kot_party (party_id),
    INDEX idx_kot_status (status),
    CONSTRAINT fk_kot_party
        FOREIGN KEY (party_id) REFERENCES party(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- KOT ITEMS (ElementCollection)
-- =====================================================
CREATE TABLE kot_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kot_id BIGINT NOT NULL,
    menu_item_id BIGINT,
    menu_item_name VARCHAR(150),
    price DOUBLE,
    quantity INT,
    notes VARCHAR(255),
    guest_number INT,
    INDEX idx_kot_items_kot (kot_id),
    INDEX idx_kot_items_menu (menu_item_id),
    CONSTRAINT fk_kot_items_kot
        FOREIGN KEY (kot_id) REFERENCES kot(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- BILL
-- =====================================================
CREATE TABLE bill (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    party_id BIGINT NOT NULL,
    subtotal DOUBLE,
    gst DOUBLE,
    total DOUBLE,
    paid_at DATETIME,
    payment_mode VARCHAR(50),
    UNIQUE KEY uk_bill_party (party_id),
    INDEX idx_bill_paid_at (paid_at),
    CONSTRAINT fk_bill_party
        FOREIGN KEY (party_id) REFERENCES party(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- FINALIZE
-- =====================================================
SET FOREIGN_KEY_CHECKS = 1;
