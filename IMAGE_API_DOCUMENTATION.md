# Image Management API Documentation

## Tổng quan

API để quản lý ảnh sản phẩm trong hệ thống Shopping System.

---

## 1. Upload ảnh cho một sản phẩm

### Endpoint

```
POST /products/upload-image/{productId}
```

### Parameters

- `productId` (path): ID của sản phẩm cần cập nhật ảnh
- `file` (form-data): File ảnh (png/jpg/jpeg)

### Ví dụ sử dụng với cURL

```bash
curl -X POST "http://localhost:8080/products/upload-image/1" \
  -F "file=@path/to/your/image.jpg"
```

### Ví dụ sử dụng với Postman

1. Method: POST
2. URL: `http://localhost:8080/products/upload-image/1`
3. Body:
   - Chọn "form-data"
   - Key: `file` (type: File)
   - Value: Chọn file ảnh từ máy tính

### Response

```json
{
  "success": true,
  "message": "Cập nhật ảnh thành công cho sản phẩm 1"
}
```

### Response lỗi

```json
{
  "success": false,
  "message": "Không thể cập nhật ảnh. Vui lòng kiểm tra file và product ID"
}
```

---

## 2. Load ảnh từ thư mục img

### Endpoint

```
POST /products/load-images
```

### Quy ước đặt tên file

Đặt file ảnh trong thư mục `img/` với tên theo format:

- `product_1.jpg` hoặc `product_1.png` cho sản phẩm ID = 1
- `product_2.jpg` hoặc `product_2.png` cho sản phẩm ID = 2
- ...

### Cấu trúc thư mục

```
shopping-sys/
├── img/
│   ├── product_1.jpg
│   ├── product_2.png
│   ├── product_3.jpg
│   └── ...
├── src/
└── pom.xml
```

### Ví dụ sử dụng với cURL

```bash
curl -X POST "http://localhost:8080/products/load-images"
```

### Response

```json
{
  "success": true,
  "message": "Đã load 10 ảnh từ thư mục img",
  "count": 10
}
```

---

## 3. Xem ảnh sản phẩm

### Endpoint

```
GET /products/image/{productId}
```

### Ví dụ

```
http://localhost:8080/products/image/1
```

Trả về file ảnh trực tiếp với content-type `image/jpeg` hoặc `image/png`

---

## Validation

- File upload phải có định dạng: `.png`, `.jpg`, `.jpeg`
- Product ID phải tồn tại trong database
- File không được rỗng

---

## Code Examples

### JavaScript (Upload ảnh)

```javascript
async function uploadProductImage(productId, file) {
  const formData = new FormData();
  formData.append("file", file);

  const response = await fetch(`/products/upload-image/${productId}`, {
    method: "POST",
    body: formData,
  });

  const result = await response.json();
  console.log(result.message);
}
```

### JavaScript (Load tất cả ảnh)

```javascript
async function loadAllImages() {
  const response = await fetch("/products/load-images", {
    method: "POST",
  });

  const result = await response.json();
  console.log(`Loaded ${result.count} images`);
}
```

---

## Notes

1. Ảnh được lưu trực tiếp vào database dạng BYTEA
2. API `/products/load-images` sẽ tự động tìm và load ảnh cho product ID từ 1-20
3. Có thể mở rộng range này trong ImageService.loadImagesFromImgFolder()
4. Nếu cả .jpg và .png đều tồn tại, .jpg sẽ được ưu tiên
