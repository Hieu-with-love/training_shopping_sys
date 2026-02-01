# Shopping System - API Testing Guide

## 📚 Mục lục

- [Giới thiệu](#giới-thiệu)
- [Cài đặt và Chạy ứng dụng](#cài-đặt-và-chạy-ứng-dụng)
- [Swagger UI - Test API](#swagger-ui---test-api)
- [Bypass Spring Security cho Testing](#bypass-spring-security-cho-testing)
- [Test API với Postman/cURL](#test-api-với-postmancurl)
- [Tài khoản mặc định](#tài-khoản-mặc-định)

---

## 🎯 Giới thiệu

Dự án Shopping System là một ứng dụng web Spring Boot với Spring Security MVC. Hướng dẫn này sẽ giúp bạn:

- Sử dụng Swagger UI để test API nhanh chóng
- Bypass authentication khi cần thiết cho môi trường development
- Test API một cách hiệu quả

---

## 🚀 Cài đặt và Chạy ứng dụng

### 1. Build project

```bash
mvn clean install
```

### 2. Chạy ứng dụng

```bash
mvn spring-boot:run
```

Hoặc:

```bash
java -jar target/shopping-sys-0.0.1-SNAPSHOT.jar
```

Ứng dụng sẽ chạy tại: `http://localhost:8080`

---

## 📖 Swagger UI - Test API

### Truy cập Swagger UI

Sau khi ứng dụng đã chạy, truy cập:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### Cách sử dụng Swagger UI

1. **Authorize**: Click vào nút "Authorize" ở góc trên bên phải
2. **Nhập credentials**:
   - Username: `admin` hoặc `user`
   - Password: `password`
3. **Test API**: Chọn endpoint và click "Try it out"

### Ví dụ test API trong Swagger:

#### 1. Lấy danh sách sản phẩm

- Endpoint: `GET /products/list`
- Không cần authentication (đã permitAll)

#### 2. Đặt hàng

- Endpoint: `POST /orders/place`
- Cần authentication
- Parameters: `productId`, `quantity`

---

## 🔓 Bypass Spring Security cho Testing

### Phương án 1: Tắt Security hoàn toàn (Chỉ dùng cho Development)

Trong file `SecurityConfig.java`, thay đổi method `securityFilterChain`:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())  // Tắt CSRF
        .authorizeHttpRequests(auth -> auth
            .anyRequest().permitAll()  // Cho phép tất cả request
        )
        .formLogin(form -> form.disable())  // Tắt form login
        .httpBasic(httpBasic -> httpBasic.disable());  // Tắt basic auth

    return http.build();
}
```

### Phương án 2: Sử dụng Profile khác nhau

#### a. Tạo file `application-dev.yaml`:

```yaml
spring:
  security:
    user:
      name: admin
      password: admin123

# Cấu hình khác cho dev...
```

#### b. Tạo class `SecurityConfigDev.java`:

```java
package com.training.shopping_sys.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("dev")
public class SecurityConfigDev {

    @Bean
    public SecurityFilterChain securityFilterChainDev(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
        return http.build();
    }
}
```

#### c. Chạy với profile dev:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Hoặc trong `application.yaml`:

```yaml
spring:
  profiles:
    active: dev
```

### Phương án 3: Bypass cho một số endpoint cụ thể

Trong `SecurityConfig.java`:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            // Swagger endpoints
            .requestMatchers(
                "/swagger-ui/**",
                "/v3/api-docs/**"
            ).permitAll()
            // API endpoints cần bypass
            .requestMatchers(
                "/api/**",           // Tất cả API endpoints
                "/products/**",       // Product endpoints
                "/orders/**"          // Order endpoints
            ).permitAll()
            // Login và static resources
            .requestMatchers("/login", "/static/**").permitAll()
            .anyRequest().authenticated()
        );

    return http.build();
}
```

### Phương án 4: Sử dụng Test User với In-Memory Authentication

Thêm vào `SecurityConfig.java`:

```java
@Bean
@Profile("dev")  // Chỉ dùng trong dev
public InMemoryUserDetailsManager testUsers() {
    UserDetails user = User.builder()
        .username("test")
        .password(passwordEncoder().encode("test"))
        .roles("USER")
        .build();

    UserDetails admin = User.builder()
        .username("testadmin")
        .password(passwordEncoder().encode("admin"))
        .roles("ADMIN", "USER")
        .build();

    return new InMemoryUserDetailsManager(user, admin);
}
```

---

## 🧪 Test API với Postman/cURL

### Sử dụng Basic Authentication

#### cURL example:

```bash
# Với Basic Auth
curl -X GET "http://localhost:8080/products/list" \
  -u admin:password

# Với session cookie (sau khi login)
curl -X POST "http://localhost:8080/orders/place" \
  -H "Cookie: JSESSIONID=your-session-id" \
  -d "productId=1&quantity=2"
```

#### Postman setup:

1. **Authorization Tab**: Chọn "Basic Auth"
2. **Username**: admin
3. **Password**: password

### Test với CSRF Disabled

Nếu bạn đã disable CSRF (trong dev), có thể POST trực tiếp:

```bash
curl -X POST "http://localhost:8080/orders/place" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "productId=1&quantity=2" \
  -u admin:password
```

---

## 👤 Tài khoản mặc định

Các tài khoản được tạo sẵn trong `DataInitializer.java`:

| Username | Password | Role  |
| -------- | -------- | ----- |
| admin    | password | ADMIN |
| user     | password | USER  |

---

## ⚙️ Cấu hình bổ sung cho Swagger

### Tùy chỉnh trong `application.yaml`:

```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    tags-sorter: alpha
    operations-sorter: alpha
    display-request-duration: true
    default-models-expand-depth: 1
    default-model-expand-depth: 1
```

### Thêm annotations vào Controller

Để có documentation tốt hơn trong Swagger:

```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Tag(name = "Products", description = "API quản lý sản phẩm")
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Operation(
        summary = "Lấy danh sách sản phẩm",
        description = "Trả về tất cả sản phẩm có trong hệ thống"
    )
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/list")
    public List<Product> getAllProducts() {
        // ...
    }
}
```

---

## 🔍 Troubleshooting

### Lỗi 401 Unauthorized khi test API

- **Giải pháp**: Kiểm tra authentication credentials hoặc bypass security như hướng dẫn ở trên

### Lỗi 403 Forbidden

- **Nguyên nhân**: CSRF protection
- **Giải pháp**: Disable CSRF trong SecurityConfig hoặc include CSRF token trong request

### Swagger UI không hiển thị

- **Kiểm tra**: URL có đúng không? `http://localhost:8080/swagger-ui.html`
- **Kiểm tra**: SecurityConfig đã permit Swagger endpoints chưa?

### API trả về HTML thay vì JSON

- **Nguyên nhân**: Request đang redirect đến login page
- **Giải pháp**:
  1. Thêm header `Accept: application/json`
  2. Bypass security cho endpoint đó
  3. Provide valid authentication

---

## 📝 Best Practices

1. **Không disable security trong production**: Chỉ bypass security trong môi trường development
2. **Sử dụng Profile**: Tách biệt config cho dev và prod
3. **Document API đầy đủ**: Sử dụng Swagger annotations
4. **Test thoroughly**: Test cả authenticated và unauthenticated endpoints
5. **Version control**: Không commit credentials thực vào git

---

## 📞 Support

Nếu gặp vấn đề, hãy:

1. Kiểm tra logs trong console
2. Verify database connection
3. Đảm bảo port 8080 không bị occupied

---

**Happy Testing! 🚀**
