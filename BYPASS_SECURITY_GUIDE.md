# 🔓 Hướng dẫn Bypass Spring Security - Step by Step

## 🎯 Mục đích

Tắt authentication tạm thời để test API nhanh trong môi trường development.

---

## ⚡ Phương pháp 1: TẮT HOÀN TOÀN (Nhanh nhất - Khuyên dùng cho dev)

### Bước 1: Mở file SecurityConfig.java

```
src/main/java/com/training/shopping_sys/config/SecurityConfig.java
```

### Bước 2: Thay thế method securityFilterChain

**TÌM đoạn code này:**

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    http
//                .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
                // Cho phép truy cập Swagger UI và OpenAPI docs
                .requestMatchers(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/swagger-resources/**",
                        "/webjars/**"
                ).permitAll()
                // Cho phép truy cập login và static resources
                .requestMatchers("/login", "/static/css/**", "/products/**").permitAll()
                .anyRequest().authenticated()
        )
        .formLogin(form -> form.disable())
        .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/products/list", true)
                .failureUrl("/login?error")
                .permitAll()
        )
        .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
        );

    return http.build();
}
```

**THAY BẰNG code này:**

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())  // Tắt CSRF
        .authorizeHttpRequests(auth -> auth
            .anyRequest().permitAll()  // Cho phép TẤT CẢ request
        )
        .formLogin(form -> form.disable())  // Tắt form login
        .httpBasic(httpBasic -> httpBasic.disable());  // Tắt basic auth

    return http.build();
}
```

### Bước 3: Restart ứng dụng

```bash
# Stop app (Ctrl+C nếu đang chạy)
# Sau đó:
mvn spring-boot:run
```

### ✅ Kết quả

- Tất cả endpoints có thể truy cập mà không cần login
- Swagger UI hoạt động ngay lập tức
- Không cần authorize trong Swagger

---

## 🎨 Phương pháp 2: Permit theo Endpoints (Linh hoạt hơn)

Nếu bạn muốn giữ security cho một số endpoints nhất định:

### Code mẫu:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            // Swagger (cho phép)
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

            // API endpoints (cho phép)
            .requestMatchers("/api/**").permitAll()
            .requestMatchers("/products/**").permitAll()
            .requestMatchers("/orders/**").permitAll()

            // Static resources (cho phép)
            .requestMatchers("/login", "/static/**", "/css/**", "/js/**").permitAll()

            // Admin endpoints (cần authentication)
            .requestMatchers("/admin/**").authenticated()

            // Các request khác (cho phép tất cả)
            .anyRequest().permitAll()
        )
        .formLogin(form -> form.disable());

    return http.build();
}
```

---

## 🔄 Phương pháp 3: Sử dụng Spring Profiles (Professional)

Tách biệt config giữa dev và prod.

### Bước 1: Tạo SecurityConfigDev.java

```java
package com.training.shopping_sys.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("dev")  // Chỉ active khi profile = dev
public class SecurityConfigDev {

    @Bean
    public SecurityFilterChain securityFilterChainDev(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            .formLogin(form -> form.disable());

        return http.build();
    }
}
```

### Bước 2: Thêm @Profile vào SecurityConfig gốc

Trong file `SecurityConfig.java`, thêm annotation:

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Profile("!dev")  // Không active khi profile = dev
public class SecurityConfig {
    // Code hiện tại...
}
```

### Bước 3: Tạo application-dev.yaml

```yaml
spring:
  profiles:
    active: dev

# Các config khác giữ nguyên...
```

### Bước 4: Run với profile dev

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Hoặc trong application.yaml:

```yaml
spring:
  profiles:
    active: dev
```

---

## 🧪 Phương pháp 4: Comment code tạm thời (Cách thủ công)

### Chỉ cần comment phần authentication:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
            .requestMatchers("/products/**").permitAll()
            // .anyRequest().authenticated()  // ← Comment dòng này
            .anyRequest().permitAll()  // ← Thêm dòng này
        )
        // Comment cả block formLogin nếu muốn
        /*
        .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/products/list", true)
                .failureUrl("/login?error")
                .permitAll()
        )
        */
        .formLogin(form -> form.disable());

    return http.build();
}
```

---

## 📋 So sánh các phương pháp

| Phương pháp               | Độ khó          | Linh hoạt  | Khuyên dùng         |
| ------------------------- | --------------- | ---------- | ------------------- |
| #1 - Tắt hoàn toàn        | ⭐ Dễ           | ❌ Thấp    | ✅ Dev/Testing      |
| #2 - Permit theo endpoint | ⭐⭐ Trung bình | ✅ Cao     | ✅ Dev/Staging      |
| #3 - Spring Profiles      | ⭐⭐⭐ Khó      | ✅ Rất cao | ✅ Production ready |
| #4 - Comment code         | ⭐ Dễ           | ❌ Thấp    | ⚠️ Tạm thời         |

---

## ✅ Checklist sau khi bypass

Sau khi thay đổi SecurityConfig, kiểm tra:

- [ ] Code compile thành công (`mvn compile`)
- [ ] Application start thành công
- [ ] Truy cập được Swagger UI: http://localhost:8080/swagger-ui.html
- [ ] Không bị redirect về login page
- [ ] API endpoints trả về data thay vì 401/403
- [ ] Thymeleaf pages load được (nếu cần)

---

## 🧪 Test nhanh

### Test với Browser:

```
http://localhost:8080/products/list
```

→ Phải thấy danh sách sản phẩm, KHÔNG redirect về /login

### Test với cURL:

```bash
curl http://localhost:8080/products/list
```

→ Phải trả về JSON/HTML, KHÔNG phải error

### Test với Swagger:

```
http://localhost:8080/swagger-ui.html
```

→ Click "Try it out" và "Execute" trực tiếp, không cần Authorize

---

## ⚠️ Lưu ý quan trọng

### ❌ KHÔNG làm trong Production:

- Tắt CSRF protection
- Permit all requests
- Disable authentication

### ✅ Nhớ làm trước khi deploy:

- Restore SecurityConfig về bản gốc
- Enable CSRF
- Require authentication cho protected endpoints
- Test security thoroughly

### 💡 Tips:

- Commit SecurityConfig gốc vào Git
- Sử dụng `.gitignore` cho local dev config
- Document các thay đổi security rõ ràng
- Sử dụng environment variables cho sensitive data

---

## 🔧 Troubleshooting

### Vẫn bị 401 Unauthorized?

✅ Check: SecurityConfig đã update đúng chưa?  
✅ Check: Application đã restart chưa?  
✅ Clear browser cache và cookies  
✅ Xem logs để tìm error message

### Vẫn bị 403 Forbidden?

✅ Tắt CSRF: `.csrf(csrf -> csrf.disable())`  
✅ Check method annotation (`@PostMapping` vs `@GetMapping`)  
✅ Check RequestMapping paths

### Swagger UI vẫn yêu cầu login?

✅ Check SecurityConfig có permit `/swagger-ui/**`?  
✅ Restart application  
✅ Try incognito/private browsing  
✅ Check port đúng không (default: 8080)

---

## 📞 Cần giúp đỡ?

Nếu vẫn gặp vấn đề:

1. Check application logs trong console
2. Verify database connection
3. Ensure port 8080 không bị occupied
4. Try với curl command để isolate issue
5. Check Spring Boot version compatibility

---

## 🎉 Hoàn thành!

Bây giờ bạn có thể:

- ✅ Test API mà không cần login
- ✅ Sử dụng Swagger UI thoải mái
- ✅ Focus vào business logic thay vì authentication
- ✅ Speed up development workflow

**Happy coding! 🚀**

---

## 📚 Tài liệu liên quan

- `README_API_TESTING.md` - Hướng dẫn test API đầy đủ
- `QUICK_START_SWAGGER.md` - Quick start với Swagger
- `SWAGGER_SETUP_SUMMARY.md` - Tổng kết setup
