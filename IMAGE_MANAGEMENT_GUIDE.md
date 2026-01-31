# Quy trình quản lý ảnh sản phẩm hoàn chỉnh

## Tổng quan kiến trúc

### Lưu trữ ảnh

- **Primary storage**: Thư mục `img/` (file system)
- **Backup storage**: Database (BYTEA column)

### Flow hiển thị ảnh

```
Browser Request → /products/image/{id}
    ↓
1. Tìm trong img/product_{id}.jpg
2. Tìm trong img/product_{id}.png
3. Nếu không có → Đọc từ DB
    ↓
Return byte[] với correct content-type
```

---

## 1. Upload ảnh cho sản phẩm

### API Endpoint

```
POST /products/upload-image/{productId}
Content-Type: multipart/form-data
```

### Quy trình upload

1. Validate file (png/jpg/jpeg)
2. Lưu file vào `img/product_{productId}.{ext}`
3. Backup byte[] vào database
4. Return success response

### Ví dụ với cURL

```bash
curl -X POST "http://localhost:8080/products/upload-image/1" \
  -F "file=@my-product-image.jpg"
```

### Ví dụ với JavaScript

```javascript
async function uploadImage(productId, file) {
  const formData = new FormData();
  formData.append("file", file);

  const response = await fetch(`/products/upload-image/${productId}`, {
    method: "POST",
    body: formData,
  });

  return await response.json();
}

// Sử dụng với input file
document.getElementById("imageInput").addEventListener("change", async (e) => {
  const file = e.target.files[0];
  const result = await uploadImage(1, file);
  console.log(result.message);
});
```

---

## 2. Load ảnh hàng loạt từ thư mục

### API Endpoint

```
POST /products/load-images
```

### Quy ước đặt tên

```
img/
├── product_1.jpg
├── product_2.png
├── product_3.jpg
└── ...
```

### Quy trình

1. Scan thư mục `img/`
2. Tìm file theo pattern `product_{id}.jpg` hoặc `product_{id}.png`
3. Đọc byte[] và lưu vào DB
4. Return số lượng ảnh đã load

---

## 3. Hiển thị ảnh trong HTML

### ProductSearchDTO

```java
public class ProductSearchDTO {
    private Long productId;
    private String productName;
    private boolean hasImage;
    private String imageUrl;  // "/products/image/1"
    // ...
}
```

### Thymeleaf Template

```html
<img
  th:if="${product.hasImage}"
  th:src="${product.imageUrl}"
  alt="Product Image"
  class="product-img"
/>
```

### Rendered HTML

```html
<img src="/products/image/1" alt="Product Image" class="product-img" />
```

---

## 4. Serve ảnh qua endpoint

### API Endpoint

```
GET /products/image/{productId}
```

### Quy trình serve

```java
1. Try: img/product_{id}.jpg
   └─> Found? Return with image/jpeg

2. Try: img/product_{id}.png
   └─> Found? Return with image/png

3. Try: Database.product_img
   └─> Found? Detect type & return

4. Not found → Return 404
```

### Response Headers

```
Content-Type: image/jpeg (or image/png)
Content-Length: {size}
Cache-Control: max-age=3600
```

---

## 5. Cấu trúc thư mục

```
shopping-sys/
├── img/                    # Image storage
│   ├── product_1.jpg
│   ├── product_2.png
│   └── ...
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── .../
│   │   │       ├── controller/
│   │   │       │   └── ProductController.java
│   │   │       ├── service/
│   │   │       │   ├── ImageService.java
│   │   │       │   └── ProductService.java
│   │   │       ├── dto/
│   │   │       │   └── ProductSearchDTO.java
│   │   │       └── util/
│   │   │           └── ImageUtil.java
│   │   └── resources/
│   │       ├── application.yaml
│   │       └── templates/
│   │           └── product-list.html
└── pom.xml
```

---

## 6. Configuration (application.yaml)

```yaml
spring:
  web:
    resources:
      static-locations: classpath:/static/,file:img/

image:
  storage:
    path: img/
```

---

## 7. Ưu điểm của kiến trúc này

✅ **Performance**

- Browser cache images (max-age=3600)
- Không query DB mỗi lần hiển thị
- Serve file trực tiếp từ file system

✅ **Reliability**

- Dual storage (file + DB)
- Fallback to DB nếu file bị mất
- Backup tự động khi upload

✅ **Scalability**

- Dễ migrate sang CDN/S3
- File system có thể mount network storage
- DB backup cho disaster recovery

✅ **Maintainability**

- Dễ backup/restore (chỉ cần copy folder img/)
- Dễ xem/quản lý files
- Clear separation of concerns

---

## 8. Testing

### Test upload

```bash
# Upload image cho product 1
curl -X POST "http://localhost:8080/products/upload-image/1" \
  -F "file=@test.jpg"

# Verify file exists
ls img/product_1.jpg
```

### Test display

```bash
# Truy cập product list
open http://localhost:8080/products/list?keyword=

# Image URL sẽ là: /products/image/1
# Browser sẽ tự động load từ endpoint
```

### Test load bulk

```bash
# Prepare files
cp image1.jpg img/product_1.jpg
cp image2.png img/product_2.png

# Load to DB
curl -X POST "http://localhost:8080/products/load-images"
```

---

## 9. Troubleshooting

### Ảnh không hiển thị

1. Check file exists: `ls img/product_{id}.jpg`
2. Check DB: `SELECT product_id, length(product_img) FROM mstproduct WHERE product_id = X;`
3. Check endpoint: `curl -I http://localhost:8080/products/image/1`
4. Check browser console for 404 errors

### Upload failed

1. Check file permission on `img/` folder
2. Check file size limit (Spring default: 1MB)
3. Check file extension (only png/jpg/jpeg allowed)
4. Check logs for detailed error

### Performance issues

1. Enable nginx/CDN for static file serving
2. Increase cache time in headers
3. Optimize image size before upload
4. Consider lazy loading for large lists

---

## 10. Future improvements

- [ ] Thumbnail generation
- [ ] Image compression on upload
- [ ] Support more formats (webp, gif)
- [ ] CDN integration
- [ ] S3/Cloud storage option
- [ ] Image cropping/editing
- [ ] Lazy loading in frontend
