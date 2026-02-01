# 🚀 Hướng dẫn Test API nhanh với Swagger

## Bước 1: Cài đặt dependencies

Dependencies đã được thêm vào `pom.xml`. Chạy lệnh:

```bash
mvn clean install
```

## Bước 2: Chạy ứng dụng

```bash
mvn spring-boot:run
```

## Bước 3: Truy cập Swagger UI

Mở trình duyệt và truy cập:

**🌐 http://localhost:8080/swagger-ui.html**

## Bước 4: Xác thực (nếu cần)

1. Click nút **"Authorize"** ở góc trên bên phải
2. Nhập:
   - Username: `admin`
   - Password: `password`
3. Click **"Authorize"** và đóng popup

## Bước 5: Test API

1. Chọn endpoint muốn test (ví dụ: `GET /products/list`)
2. Click **"Try it out"**
3. Điền tham số (nếu cần)
4. Click **"Execute"**
5. Xem kết quả trong phần **Response**

---

## 🔓 Bypass Login để Test nhanh hơn

### Option 1: Tắt Security tạm thời (Dễ nhất)

Trong file `SecurityConfig.java`, thay method `securityFilterChain`:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .formLogin(form -> form.disable());
    return http.build();
}
```

### Option 2: Permit tất cả API endpoints

Giữ nguyên security cho web UI, chỉ permit API:

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
    .requestMatchers("/api/**").permitAll()  // Permit all API
    .requestMatchers("/products/**").permitAll()
    .requestMatchers("/orders/**").permitAll()
    .requestMatchers("/login", "/static/**").permitAll()
    .anyRequest().authenticated()
)
```

### Option 3: Sử dụng Profile Development

Tạo file `application-dev.yaml`:

```yaml
spring:
  profiles:
    active: dev
  security:
    user:
      name: dev
      password: dev
```

Chạy với profile dev:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## 📋 Endpoints chính

| Method | Endpoint               | Mô tả              |
| ------ | ---------------------- | ------------------ |
| GET    | `/products/list`       | Danh sách sản phẩm |
| GET    | `/products/search`     | Tìm kiếm sản phẩm  |
| GET    | `/products/{id}/stock` | Kiểm tra tồn kho   |
| POST   | `/orders/place`        | Đặt hàng           |
| GET    | `/orders/history`      | Lịch sử đơn hàng   |

---

## ⚡ Test nhanh với cURL

```bash
# Lấy danh sách sản phẩm
curl http://localhost:8080/products/list

# Đặt hàng (với Basic Auth)
curl -X POST "http://localhost:8080/orders/place?productId=1&quantity=2" \
  -u admin:password

# Kiểm tra tồn kho
curl "http://localhost:8080/products/1/stock?quantity=5"
```

---

## 🎯 Tips

1. **Swagger UI tự động refresh** sau khi restart app
2. **Tắt CSRF** nếu gặp lỗi 403: `.csrf(csrf -> csrf.disable())`
3. **Xem logs** để debug: Console sẽ hiện SQL queries
4. **Authentication persists**: Sau khi authorize trong Swagger, không cần authorize lại cho mỗi request

---

## ❗ Troubleshooting

| Vấn đề             | Giải pháp                                               |
| ------------------ | ------------------------------------------------------- |
| 401 Unauthorized   | Bypass security hoặc authorize trong Swagger            |
| 403 Forbidden      | Disable CSRF protection                                 |
| Swagger không load | Check SecurityConfig đã permit `/swagger-ui/**`         |
| Port 8080 đã dùng  | Change port trong application.yaml: `server.port: 8081` |

---

## 📚 Tài liệu chi tiết

Xem file `README_API_TESTING.md` để có hướng dẫn đầy đủ và chi tiết hơn.

---

**Chúc bạn test API thành công! 🎉**
