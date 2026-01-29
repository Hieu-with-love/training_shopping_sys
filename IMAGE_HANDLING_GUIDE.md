# Hướng dẫn xử lý ảnh dạng byte[] trong Shopping System

## Thay đổi đã thực hiện

### 1. Entity Layer (MstProduct.java)

- Đổi field `productImg` từ `String` sang `byte[]`
- Thêm annotation `@Lob` và `columnDefinition = "LONGBLOB"` để lưu ảnh dạng binary trong MySQL

```java
@Lob
@Column(name = "product_img", columnDefinition = "LONGBLOB")
private byte[] productImg;
```

### 2. DTO Layer (ProductSearchDTO.java)

- Đổi từ `String productImg` sang `boolean hasImage`
- Chỉ cần biết sản phẩm có ảnh hay không, không cần truyền toàn bộ byte[] trong DTO

### 3. Controller Layer (ProductController.java)

- Thêm endpoint mới `GET /products/image/{productId}` để trả ảnh
- Endpoint này trả về `ResponseEntity<byte[]>` với `Content-Type: image/jpeg`
- Frontend có thể gọi endpoint này trong thẻ `<img>`

```java
@GetMapping("/image/{productId}")
public ResponseEntity<byte[]> getProductImage(@PathVariable Long productId) {
    // Lấy ảnh từ database và trả về dạng byte[]
}
```

### 4. Service Layer (ProductService.java)

- Xử lý `byte[]` từ database
- Set `hasImage = true` nếu byte array không null và có dữ liệu

### 5. View Layer (product-list.html)

- Sử dụng Thymeleaf để tạo URL động: `th:src="@{'/products/image/' + ${product.productId}}"`
- Thẻ img sẽ gọi endpoint `/products/image/{productId}` để lấy ảnh

```html
<img
  th:if="${product.hasImage}"
  th:src="@{'/products/image/' + ${product.productId}}"
  alt="Product Image"
  class="product-img"
/>
```

## Cách test với ảnh thật

### Option 1: Sử dụng script SQL có sẵn (ảnh mẫu nhỏ)

```sql
-- File: db-init-with-images.sql
-- Chứa ảnh pixel 1x1 màu đỏ, xanh, vàng để test
```

Execute script:

```bash
mysql -u root -p shopping_db < src/main/resources/db-init-with-images.sql
```

### Option 2: Generate SQL với ảnh thật (Khuyến nghị)

1. **Cài đặt Python và Pillow:**

```bash
pip install Pillow
```

2. **Chạy script generate:**

```bash
cd src/main/resources
python generate_sql_with_images.py
```

Script sẽ:

- Tạo folder `images/` nếu chưa có
- Generate 10 ảnh màu mẫu (100x100 pixels) nếu chưa có ảnh
- Tạo file `db-init-with-real-images.sql` chứa dữ liệu ảnh thật

3. **Hoặc sử dụng ảnh của bạn:**

- Đặt ảnh vào folder `images/`
- Đặt tên: `product1.jpg`, `product2.jpg`, ..., `product10.jpg`
- Chạy lại script

4. **Execute SQL generated:**

```bash
mysql -u root -p shopping_db < src/main/resources/db-init-with-real-images.sql
```

### Option 3: Insert ảnh trực tiếp bằng MySQL LOAD_FILE()

Nếu có quyền FILE privilege:

```sql
UPDATE mstproduct
SET product_img = LOAD_FILE('/path/to/image.jpg')
WHERE product_id = 1;
```

**Lưu ý:** Path phải là absolute path trên server MySQL

## Kiểm tra kết quả

1. **Start Spring Boot application:**

```bash
mvn spring-boot:run
```

2. **Login vào hệ thống:**

- URL: http://localhost:8080/login
- Username: admin / Password: admin123

3. **Truy cập trang danh sách sản phẩm:**

- URL: http://localhost:8080/products/list
- Nhấn "Tìm kiếm" để hiển thị tất cả sản phẩm

4. **Kiểm tra ảnh:**

- Ảnh sẽ hiển thị trong cột "Hình ảnh"
- Check browser DevTools Network tab để xem request `/products/image/{id}`
- Response headers phải có `Content-Type: image/jpeg`

## Verify trong database

```sql
-- Kiểm tra size của ảnh (bytes)
SELECT
    product_id,
    product_name,
    LENGTH(product_img) as image_size_bytes,
    CASE
        WHEN product_img IS NULL THEN 'No Image'
        ELSE 'Has Image'
    END as image_status
FROM mstproduct;
```

## Troubleshooting

### Ảnh không hiển thị

1. Check database: `SELECT LENGTH(product_img) FROM mstproduct WHERE product_id = 1;`
2. Check endpoint: Truy cập trực tiếp `http://localhost:8080/products/image/1`
3. Check browser console có lỗi không
4. Check Content-Type header = `image/jpeg`

### Script Python lỗi

- Cài đặt Pillow: `pip install Pillow`
- Check Python version >= 3.6

### MySQL error: Packet too large

```sql
-- Tăng max_allowed_packet
SET GLOBAL max_allowed_packet=16777216; -- 16MB
```

### Ảnh quá lớn

- Nên resize ảnh trước khi lưu (khuyến nghị < 500KB)
- Có thể dùng ImageMagick hoặc Pillow để resize

## Performance Tips

1. **Lazy Loading:** Chỉ load ảnh khi cần (đã implement qua endpoint riêng)
2. **Caching:** Có thể thêm `@Cacheable` cho endpoint `/products/image/{id}`
3. **Thumbnail:** Lưu thêm field `product_thumbnail` (byte[]) để hiển thị nhanh
4. **CDN:** Trong production, nên lưu ảnh trên CDN/File Storage thay vì database

## Lưu ý quan trọng

- **Database size:** Lưu ảnh trong database sẽ làm database size tăng nhanh
- **Backup:** Backup database sẽ lâu hơn
- **Recommendation:** Với production, nên lưu ảnh trên file storage (S3, Azure Blob, etc.) và chỉ lưu URL trong database
- **Current approach:** Phù hợp cho demo, testing, hoặc hệ thống nhỏ
