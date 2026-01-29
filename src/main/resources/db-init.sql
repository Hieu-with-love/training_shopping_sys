-- ============================================
-- Script tạo database và dữ liệu mẫu
-- Shopping System - Product List
-- ============================================

-- Tạo database (chạy lệnh này trước nếu chưa có database)
-- CREATE DATABASE shopping_sys;

-- Kết nối vào database shopping_sys và chạy các lệnh sau

-- ============================================
-- 1. Tạo bảng mstproducttype (Loại sản phẩm)
-- ============================================
DROP TABLE IF EXISTS trproductorder CASCADE;
DROP TABLE IF EXISTS mstproduct CASCADE;
DROP TABLE IF EXISTS mstproducttype CASCADE;

CREATE TABLE mstproducttype (
    producttype_id SERIAL PRIMARY KEY,
    producttype_name VARCHAR(400) NOT NULL,
    status VARCHAR(1) DEFAULT '0' -- '0' = active, '1' = deleted
);

-- ============================================
-- 2. Tạo bảng mstproduct (Sản phẩm)
-- ============================================
CREATE TABLE mstproduct (
    product_id SERIAL PRIMARY KEY,
    product_name VARCHAR(400) NOT NULL,
    product_description VARCHAR(400),
    product_img VARCHAR(500),
    producttype_id INTEGER NOT NULL,
    status VARCHAR(1) DEFAULT '0', -- '0' = active, '1' = deleted
    CONSTRAINT fk_producttype FOREIGN KEY (producttype_id) 
        REFERENCES mstproducttype(producttype_id)
);

-- ============================================
-- 3. Tạo bảng trproductorder (Đơn hàng)
-- ============================================
CREATE TABLE trproductorder (
    order_id SERIAL PRIMARY KEY,
    product_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    user_id INTEGER,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_product FOREIGN KEY (product_id) 
        REFERENCES mstproduct(product_id)
);

-- ============================================
-- 4. Insert dữ liệu mẫu - Loại sản phẩm
-- ============================================
INSERT INTO mstproducttype (producttype_name, status) VALUES
('Điện thoại', '0'),
('Laptop', '0'),
('Tablet', '0'),
('Phụ kiện', '0'),
('Tai nghe', '0'),
('Đồng hồ thông minh', '0'),
('Loại đã xóa', '1'); -- Loại này sẽ không hiển thị

-- ============================================
-- 5. Insert dữ liệu mẫu - Sản phẩm
-- ============================================

-- Điện thoại (producttype_id = 1)
INSERT INTO mstproduct (product_name, product_description, product_img, producttype_id, status) VALUES
('iPhone 15 Pro Max', 'Điện thoại cao cấp với chip A17 Pro, camera 48MP', 'https://via.placeholder.com/150/0000FF/FFFFFF?text=iPhone+15', 1, '0'),
('Samsung Galaxy S24 Ultra', 'Flagship Android với bút S-Pen, màn hình Dynamic AMOLED', 'https://via.placeholder.com/150/FF0000/FFFFFF?text=Samsung+S24', 1, '0'),
('Xiaomi 14 Pro', 'Điện thoại Xiaomi cao cấp với Snapdragon 8 Gen 3', 'https://via.placeholder.com/150/00FF00/FFFFFF?text=Xiaomi+14', 1, '0'),
('iPhone 14 Pro', 'Thế hệ trước của iPhone với Dynamic Island', 'https://via.placeholder.com/150/0000FF/FFFFFF?text=iPhone+14', 1, '0'),
('OPPO Find X6 Pro', 'Điện thoại OPPO cao cấp với camera Hasselblad', 'https://via.placeholder.com/150/FFA500/FFFFFF?text=OPPO+X6', 1, '0'),
('Vivo X100 Pro', 'Điện thoại Vivo với camera ZEISS', 'https://via.placeholder.com/150/800080/FFFFFF?text=Vivo+X100', 1, '0'),

-- Laptop (producttype_id = 2)
('MacBook Pro 16 M3', 'Laptop Apple với chip M3 Max, màn hình Liquid Retina XDR', 'https://via.placeholder.com/150/808080/FFFFFF?text=MacBook+M3', 2, '0'),
('Dell XPS 15', 'Laptop cao cấp cho doanh nhân và sáng tạo nội dung', 'https://via.placeholder.com/150/000080/FFFFFF?text=Dell+XPS', 2, '0'),
('Lenovo ThinkPad X1 Carbon', 'Laptop doanh nghiệp mỏng nhẹ, bền bỉ', 'https://via.placeholder.com/150/8B0000/FFFFFF?text=ThinkPad', 2, '0'),
('ASUS ROG Zephyrus G16', 'Laptop gaming cao cấp với RTX 4080', 'https://via.placeholder.com/150/FF1493/FFFFFF?text=ROG+G16', 2, '0'),
('HP Spectre x360', 'Laptop 2-in-1 cao cấp với thiết kế sang trọng', 'https://via.placeholder.com/150/4169E1/FFFFFF?text=HP+Spectre', 2, '0'),

-- Tablet (producttype_id = 3)
('iPad Pro 12.9 M2', 'Tablet cao cấp với chip M2, màn hình Liquid Retina XDR', 'https://via.placeholder.com/150/C0C0C0/000000?text=iPad+Pro', 3, '0'),
('Samsung Galaxy Tab S9 Ultra', 'Tablet Android cao cấp với màn hình AMOLED lớn', 'https://via.placeholder.com/150/FF6347/FFFFFF?text=Tab+S9', 3, '0'),
('iPad Air M1', 'Tablet Apple giá tốt với chip M1', 'https://via.placeholder.com/150/87CEEB/000000?text=iPad+Air', 3, '0'),

-- Phụ kiện (producttype_id = 4)
('Apple Pencil 2', 'Bút cảm ứng cho iPad với sạc không dây', 'https://via.placeholder.com/150/FFFFFF/000000?text=Pencil+2', 4, '0'),
('Magic Keyboard', 'Bàn phím cao cấp cho iPad Pro', 'https://via.placeholder.com/150/000000/FFFFFF?text=Magic+KB', 4, '0'),
('Samsung S-Pen Pro', 'Bút cảm ứng đa năng cho thiết bị Samsung', 'https://via.placeholder.com/150/4682B4/FFFFFF?text=S-Pen', 4, '0'),

-- Tai nghe (producttype_id = 5)
('AirPods Pro 2', 'Tai nghe không dây cao cấp với chống ồn chủ động', 'https://via.placeholder.com/150/F5F5DC/000000?text=AirPods', 5, '0'),
('Sony WH-1000XM5', 'Tai nghe chụp tai với chống ồn hàng đầu', 'https://via.placeholder.com/150/2F4F4F/FFFFFF?text=Sony+XM5', 5, '0'),
('Samsung Galaxy Buds2 Pro', 'Tai nghe không dây cao cấp của Samsung', 'https://via.placeholder.com/150/191970/FFFFFF?text=Buds2+Pro', 5, '0'),
('Bose QuietComfort Ultra', 'Tai nghe Bose với chất lượng âm thanh tuyệt vời', 'https://via.placeholder.com/150/8B4513/FFFFFF?text=Bose+QC', 5, '0'),

-- Đồng hồ thông minh (producttype_id = 6)
('Apple Watch Ultra 2', 'Đồng hồ thông minh cao cấp cho thể thao và phiêu lưu', 'https://via.placeholder.com/150/FF4500/FFFFFF?text=Watch+Ultra', 6, '0'),
('Samsung Galaxy Watch 6 Classic', 'Đồng hồ thông minh Android cao cấp', 'https://via.placeholder.com/150/20B2AA/FFFFFF?text=Watch+6', 6, '0'),
('Garmin Fenix 7', 'Đồng hồ thể thao chuyên nghiệp', 'https://via.placeholder.com/150/556B2F/FFFFFF?text=Fenix+7', 6, '0'),

-- Sản phẩm đã xóa (status = '1') - không hiển thị
('Sản phẩm đã xóa 1', 'Sản phẩm này đã bị xóa', 'https://via.placeholder.com/150', 1, '1'),
('Sản phẩm đã xóa 2', 'Sản phẩm này đã bị xóa', 'https://via.placeholder.com/150', 2, '1');

-- ============================================
-- 6. Insert dữ liệu mẫu - Đơn hàng
-- ============================================
-- Tạo đơn hàng để test sắp xếp theo số lượng

-- iPhone 15 Pro Max - 150 đơn
INSERT INTO trproductorder (product_id, quantity, user_id) VALUES
(1, 50, 1), (1, 30, 2), (1, 40, 3), (1, 30, 4);

-- Samsung Galaxy S24 Ultra - 200 đơn
INSERT INTO trproductorder (product_id, quantity, user_id) VALUES
(2, 80, 1), (2, 60, 2), (2, 60, 3);

-- Xiaomi 14 Pro - 80 đơn
INSERT INTO trproductorder (product_id, quantity, user_id) VALUES
(3, 30, 1), (3, 25, 2), (3, 25, 3);

-- iPhone 14 Pro - 120 đơn
INSERT INTO trproductorder (product_id, quantity, user_id) VALUES
(4, 50, 1), (4, 40, 2), (4, 30, 3);

-- MacBook Pro 16 M3 - 95 đơn
INSERT INTO trproductorder (product_id, quantity, user_id) VALUES
(7, 45, 1), (7, 50, 2);

-- Dell XPS 15 - 110 đơn
INSERT INTO trproductorder (product_id, quantity, user_id) VALUES
(8, 60, 1), (8, 50, 2);

-- iPad Pro - 140 đơn
INSERT INTO trproductorder (product_id, quantity, user_id) VALUES
(12, 70, 1), (12, 70, 2);

-- AirPods Pro 2 - 300 đơn (sản phẩm bán chạy nhất)
INSERT INTO trproductorder (product_id, quantity, user_id) VALUES
(17, 100, 1), (17, 100, 2), (17, 100, 3);

-- Apple Watch Ultra 2 - 180 đơn
INSERT INTO trproductorder (product_id, quantity, user_id) VALUES
(21, 90, 1), (21, 90, 2);

-- Các sản phẩm còn lại có ít đơn hơn hoặc không có đơn

-- ============================================
-- 7. Tạo bảng users nếu chưa có (cho authentication)
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE
);

-- Insert user mẫu (password: "123456" đã mã hóa bằng BCrypt)
-- Bạn cần chạy ứng dụng để BCrypt mã hóa password hoặc dùng online tool
INSERT INTO users (username, password, role, enabled) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_ADMIN', true),
('user', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_USER', true);
-- Username: admin / Password: 123456
-- Username: user / Password: 123456

-- ============================================
-- 8. Kiểm tra dữ liệu
-- ============================================

-- Kiểm tra số lượng sản phẩm theo loại
SELECT 
    pt.producttype_name,
    COUNT(p.product_id) as total_products
FROM mstproducttype pt
LEFT JOIN mstproduct p ON pt.producttype_id = p.producttype_id AND p.status != '1'
WHERE pt.status != '1'
GROUP BY pt.producttype_id, pt.producttype_name
ORDER BY pt.producttype_name;

-- Kiểm tra sản phẩm và tổng số lượng đặt hàng (sắp xếp theo số lượng)
SELECT 
    p.product_id,
    p.product_name,
    pt.producttype_name,
    COALESCE(SUM(o.quantity), 0) as total_ordered
FROM mstproduct p
INNER JOIN mstproducttype pt ON p.producttype_id = pt.producttype_id
LEFT JOIN trproductorder o ON p.product_id = o.product_id
WHERE p.status != '1' AND pt.status != '1'
GROUP BY p.product_id, p.product_name, pt.producttype_name
ORDER BY total_ordered ASC;

-- ============================================
-- HƯỚNG DẪN SỬ DỤNG:
-- ============================================
-- 1. Mở pgAdmin hoặc psql
-- 2. Kết nối đến PostgreSQL server
-- 3. Tạo database: CREATE DATABASE shopping_sys;
-- 4. Kết nối vào database shopping_sys
-- 5. Chạy toàn bộ script này
-- 6. Khởi động Spring Boot application
-- 7. Truy cập: http://localhost:8080/login
-- 8. Đăng nhập với: admin / 123456
-- 9. Hệ thống sẽ redirect đến: http://localhost:8080/products/list
-- 10. Test các chức năng tìm kiếm và phân trang

-- ============================================
-- TEST CASES:
-- ============================================
-- 1. Tìm kiếm "iPhone" -> Sẽ trả về 2 sản phẩm (iPhone 15, iPhone 14)
-- 2. Tìm kiếm "camera" -> Sẽ trả về các sản phẩm có "camera" trong mô tả
-- 3. Chọn loại "Điện thoại" -> Sẽ trả về 6 sản phẩm điện thoại
-- 4. Chọn loại "Tai nghe" -> Sẽ trả về 4 sản phẩm tai nghe
-- 5. Tìm kiếm "cao cấp" -> Sẽ trả về nhiều sản phẩm có từ "cao cấp"
-- 6. Không nhập gì -> Hiển thị thông báo "Vui lòng nhập điều kiện tìm kiếm"
-- 7. Test phân trang với keyword "cao cấp" (nhiều hơn 5 kết quả)
-- 8. Sắp xếp: Sản phẩm có ít đơn hàng sẽ hiển thị trước

-- ============================================
-- KẾT QUẢ MONG ĐỢI:
-- ============================================
-- Sản phẩm sẽ được sắp xếp theo số lượng đặt hàng tăng dần:
-- 1. Các sản phẩm chưa có đơn (0 đơn)
-- 2. Xiaomi 14 Pro (80 đơn)
-- 3. MacBook Pro 16 M3 (95 đơn)
-- 4. Dell XPS 15 (110 đơn)
-- 5. iPhone 14 Pro (120 đơn)
-- 6. iPad Pro (140 đơn)
-- 7. iPhone 15 Pro Max (150 đơn)
-- 8. Apple Watch Ultra 2 (180 đơn)
-- 9. Samsung Galaxy S24 Ultra (200 đơn)
-- 10. AirPods Pro 2 (300 đơn) - Bán chạy nhất
