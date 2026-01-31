-- ============================================
-- Script migration: Thêm product_amount và tạo lại bảng trproductorder
-- Shopping System - Migration Script
-- ============================================

-- ============================================
-- 1. Xóa bảng trproductorder cũ để tạo lại với composite key mới
-- ============================================
DROP TABLE IF EXISTS trproductorder CASCADE;

-- ============================================
-- 2. Thêm cột product_amount vào bảng mstproduct
-- ============================================
ALTER TABLE mstproduct ADD COLUMN IF NOT EXISTS product_amount INTEGER DEFAULT 100;

-- ============================================
-- 3. Cập nhật product_amount cho các sản phẩm hiện có
-- ============================================
UPDATE mstproduct SET product_amount = 100 WHERE product_amount IS NULL;

-- Đặt các giá trị khác nhau cho từng sản phẩm để test
UPDATE mstproduct SET product_amount = 150 WHERE product_id = 1;
UPDATE mstproduct SET product_amount = 200 WHERE product_id = 2;
UPDATE mstproduct SET product_amount = 80 WHERE product_id = 3;
UPDATE mstproduct SET product_amount = 120 WHERE product_id = 4;
UPDATE mstproduct SET product_amount = 90 WHERE product_id = 5;
UPDATE mstproduct SET product_amount = 250 WHERE product_id = 6;
UPDATE mstproduct SET product_amount = 180 WHERE product_id = 7;
UPDATE mstproduct SET product_amount = 110 WHERE product_id = 8;
UPDATE mstproduct SET product_amount = 140 WHERE product_id = 9;
UPDATE mstproduct SET product_amount = 95 WHERE product_id = 10;

-- ============================================
-- 4. Tạo lại bảng trproductorder với composite key
-- ============================================
CREATE TABLE trproductorder (
    order_id BIGINT NOT NULL,
    customer_name VARCHAR(400) NOT NULL,
    product_id INTEGER NOT NULL,
    order_product_amount INTEGER NOT NULL DEFAULT 0,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (order_id, customer_name, product_id),
    CONSTRAINT fk_product FOREIGN KEY (product_id) 
        REFERENCES mstproduct(product_id)
);

-- ============================================
-- 5. Thêm dữ liệu test cho bảng trproductorder
-- ============================================

-- Customer: user1
INSERT INTO trproductorder (order_id, customer_name, product_id, order_product_amount, order_date) 
VALUES (1001, 'user1', 1, 10, NOW() - INTERVAL '10 days');

INSERT INTO trproductorder (order_id, customer_name, product_id, order_product_amount, order_date) 
VALUES (1002, 'user1', 2, 5, NOW() - INTERVAL '9 days');

INSERT INTO trproductorder (order_id, customer_name, product_id, order_product_amount, order_date) 
VALUES (1003, 'user1', 3, 15, NOW() - INTERVAL '8 days');

-- Customer: user2
INSERT INTO trproductorder (order_id, customer_name, product_id, order_product_amount, order_date) 
VALUES (1004, 'user2', 1, 20, NOW() - INTERVAL '7 days');

INSERT INTO trproductorder (order_id, customer_name, product_id, order_product_amount, order_date) 
VALUES (1005, 'user2', 4, 8, NOW() - INTERVAL '6 days');

INSERT INTO trproductorder (order_id, customer_name, product_id, order_product_amount, order_date) 
VALUES (1006, 'user2', 2, 12, NOW() - INTERVAL '5 days');

-- Customer: user3
INSERT INTO trproductorder (order_id, customer_name, product_id, order_product_amount, order_date) 
VALUES (1007, 'user3', 3, 25, NOW() - INTERVAL '4 days');

INSERT INTO trproductorder (order_id, customer_name, product_id, order_product_amount, order_date) 
VALUES (1008, 'user3', 5, 7, NOW() - INTERVAL '3 days');

INSERT INTO trproductorder (order_id, customer_name, product_id, order_product_amount, order_date) 
VALUES (1009, 'user3', 1, 18, NOW() - INTERVAL '2 days');

-- Customer: admin
INSERT INTO trproductorder (order_id, customer_name, product_id, order_product_amount, order_date) 
VALUES (1010, 'admin', 6, 30, NOW() - INTERVAL '1 day');

INSERT INTO trproductorder (order_id, customer_name, product_id, order_product_amount, order_date) 
VALUES (1011, 'admin', 7, 22, NOW() - INTERVAL '12 hours');

INSERT INTO trproductorder (order_id, customer_name, product_id, order_product_amount, order_date) 
VALUES (1012, 'admin', 2, 14, NOW() - INTERVAL '6 hours');

-- Customer: customer1
INSERT INTO trproductorder (order_id, customer_name, product_id, order_product_amount, order_date) 
VALUES (1013, 'customer1', 8, 9, NOW() - INTERVAL '3 hours');

INSERT INTO trproductorder (order_id, customer_name, product_id, order_product_amount, order_date) 
VALUES (1014, 'customer1', 9, 16, NOW() - INTERVAL '2 hours');

INSERT INTO trproductorder (order_id, customer_name, product_id, order_product_amount, order_date) 
VALUES (1015, 'customer1', 10, 11, NOW() - INTERVAL '1 hour');

-- Thêm nhiều orders cho cùng sản phẩm để test tổng số lượng
INSERT INTO trproductorder (order_id, customer_name, product_id, order_product_amount, order_date) 
VALUES (1016, 'user1', 1, 5, NOW() - INTERVAL '30 minutes');

INSERT INTO trproductorder (order_id, customer_name, product_id, order_product_amount, order_date) 
VALUES (1017, 'customer1', 1, 12, NOW() - INTERVAL '15 minutes');

INSERT INTO trproductorder (order_id, customer_name, product_id, order_product_amount, order_date) 
VALUES (1018, 'user2', 3, 20, NOW() - INTERVAL '10 minutes');

INSERT INTO trproductorder (order_id, customer_name, product_id, order_product_amount, order_date) 
VALUES (1019, 'user3', 2, 8, NOW() - INTERVAL '5 minutes');

-- ============================================
-- 6. Kiểm tra kết quả
-- ============================================

-- Xem tổng số lượng đã order cho mỗi sản phẩm (sắp xếp giảm dần)
SELECT 
    p.product_id,
    p.product_name,
    p.product_amount as total_stock,
    COALESCE(SUM(o.order_product_amount), 0) as total_ordered,
    p.product_amount - COALESCE(SUM(o.order_product_amount), 0) as available_stock
FROM mstproduct p
LEFT JOIN trproductorder o ON p.product_id = o.product_id
WHERE p.status = '0'
GROUP BY p.product_id, p.product_name, p.product_amount
ORDER BY total_ordered DESC;

-- Xem chi tiết các orders
SELECT 
    o.order_id,
    o.customer_name,
    p.product_name,
    o.order_product_amount,
    o.order_date
FROM trproductorder o
JOIN mstproduct p ON o.product_id = p.product_id
ORDER BY o.order_date DESC;
