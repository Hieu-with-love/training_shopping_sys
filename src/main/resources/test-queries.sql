-- ============================================
-- Script test để verify các query sau khi migration
-- Shopping System - Test Queries
-- ============================================

-- Test 1: Kiểm tra cấu trúc bảng trproductorder
\d trproductorder

-- Test 2: Kiểm tra cấu trúc bảng mstproduct (có product_amount chưa)
\d mstproduct

-- Test 3: Query trong MstProductRepository.searchProducts()
-- Kiểm tra query search với SUM(o.order_product_amount)
SELECT 
    p.product_id, 
    p.product_name, 
    p.product_description, 
    p.product_img IS NOT NULL as has_image,
    p.producttype_id, 
    pt.producttype_name, 
    COALESCE(SUM(o.order_product_amount), 0) as total_ordered 
FROM mstproduct p 
INNER JOIN mstproducttype pt ON p.producttype_id = pt.producttype_id 
LEFT JOIN trproductorder o ON p.product_id = o.product_id 
WHERE p.status != '1' 
AND pt.status != '1' 
GROUP BY p.product_id, p.product_name, p.product_description, p.product_img, p.producttype_id, pt.producttype_name 
ORDER BY total_ordered DESC;

-- Test 4: Query trong TrProductOrderRepository.getTotalOrderedAmount()
-- Kiểm tra tổng số lượng đã order cho từng sản phẩm
SELECT 
    product_id,
    COALESCE(SUM(order_product_amount), 0) as total_ordered
FROM trproductorder
GROUP BY product_id
ORDER BY total_ordered DESC;

-- Test 5: Query với keyword search
SELECT 
    p.product_id, 
    p.product_name, 
    p.product_description, 
    p.producttype_id, 
    pt.producttype_name, 
    COALESCE(SUM(o.order_product_amount), 0) as total_ordered 
FROM mstproduct p 
INNER JOIN mstproducttype pt ON p.producttype_id = pt.producttype_id 
LEFT JOIN trproductorder o ON p.product_id = o.product_id 
WHERE p.status != '1' 
AND pt.status != '1' 
AND (p.product_name ILIKE '%laptop%' OR p.product_description ILIKE '%laptop%')
GROUP BY p.product_id, p.product_name, p.product_description, p.product_img, p.producttype_id, pt.producttype_name 
ORDER BY total_ordered DESC;

-- Test 6: Query với producttype filter
SELECT 
    p.product_id, 
    p.product_name, 
    p.product_description, 
    p.producttype_id, 
    pt.producttype_name, 
    COALESCE(SUM(o.order_product_amount), 0) as total_ordered 
FROM mstproduct p 
INNER JOIN mstproducttype pt ON p.producttype_id = pt.producttype_id 
LEFT JOIN trproductorder o ON p.product_id = o.product_id 
WHERE p.status != '1' 
AND pt.status != '1' 
AND p.producttype_id = 1
GROUP BY p.product_id, p.product_name, p.product_description, p.product_img, p.producttype_id, pt.producttype_name 
ORDER BY total_ordered DESC;

-- Test 7: Query validate stock (ProductController.validateStock)
-- Kiểm tra số lượng còn lại trong kho cho product_id = 1
SELECT 
    p.product_id,
    p.product_name,
    p.product_amount as total_stock,
    COALESCE(SUM(o.order_product_amount), 0) as total_ordered,
    p.product_amount - COALESCE(SUM(o.order_product_amount), 0) as available_stock
FROM mstproduct p
LEFT JOIN trproductorder o ON p.product_id = o.product_id
WHERE p.product_id = 1
GROUP BY p.product_id, p.product_name, p.product_amount;

-- Test 8: Verify composite key hoạt động đúng
-- Kiểm tra có thể insert nhiều orders với cùng order_id nhưng khác customer hoặc product
INSERT INTO trproductorder (order_id, customer_name, product_id, order_product_amount, order_date) 
VALUES (9999, 'test_user1', 1, 5, NOW());

INSERT INTO trproductorder (order_id, customer_name, product_id, order_product_amount, order_date) 
VALUES (9999, 'test_user2', 1, 3, NOW());

INSERT INTO trproductorder (order_id, customer_name, product_id, order_product_amount, order_date) 
VALUES (9999, 'test_user1', 2, 7, NOW());

-- Verify inserted
SELECT * FROM trproductorder WHERE order_id = 9999;

-- Test 9: Verify không thể insert duplicate composite key
-- Query này phải fail
-- INSERT INTO trproductorder (order_id, customer_name, product_id, order_product_amount, order_date) 
-- VALUES (9999, 'test_user1', 1, 10, NOW());

-- Test 10: Clean up test data
DELETE FROM trproductorder WHERE order_id = 9999;

-- Test 11: Top 5 sản phẩm bán chạy nhất
SELECT 
    p.product_id,
    p.product_name,
    COALESCE(SUM(o.order_product_amount), 0) as total_sold
FROM mstproduct p
LEFT JOIN trproductorder o ON p.product_id = o.product_id
WHERE p.status = '0'
GROUP BY p.product_id, p.product_name
ORDER BY total_sold DESC
LIMIT 5;

-- Test 12: Sản phẩm gần hết hàng (available_stock < 20)
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
HAVING p.product_amount - COALESCE(SUM(o.order_product_amount), 0) < 20
ORDER BY available_stock ASC;

-- ============================================
-- Kết quả mong đợi:
-- ============================================
-- Test 3: Sản phẩm sắp xếp theo total_ordered DESC (nhiều nhất ở trên)
-- Test 4: Tổng số lượng order của từng sản phẩm
-- Test 5: Tìm kiếm theo keyword
-- Test 6: Lọc theo loại sản phẩm
-- Test 7: Hiển thị số lượng còn lại trong kho
-- Test 8: Insert thành công 3 records
-- Test 11: Top 5 sản phẩm bán chạy
-- Test 12: Danh sách sản phẩm gần hết hàng
