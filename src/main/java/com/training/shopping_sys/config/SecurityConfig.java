package com.training.shopping_sys.config;

import com.training.shopping_sys.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration.
 * 
 * <p>Configures Spring Security for the shopping system including:
 * - HTTP request authorization rules
 * - Form-based login configuration
 * - Password encoding with BCrypt
 * - User authentication provider
 * </p>
 * 
 * <p>Security rules:
 * - Public access: Login page, static resources, Swagger UI, product APIs
 * - Authenticated access: Order operations
 * - CSRF disabled for development
 * </p>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    /**
     * Configure security filter chain.
     * 
     * <p>Defines authorization rules for different URL patterns:
     * - Permits public access to Swagger, login, and product endpoints
     * - Requires authentication for order operations
     * - Configures form login with custom success/failure URLs
     * - Configures logout behavior
     * </p>
     * 
     * @param http HttpSecurity object to configure
     * @return Configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())  // Tắt CSRF cho development
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
                        // Cho phép truy cập orders (yêu cầu authentication)
                        .requestMatchers("/orders/**").authenticated()
                        .anyRequest().authenticated()
                )
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

    /**
     * Configure password encoder.
     * 
     * <p>Uses BCrypt hashing algorithm for password encryption.
     * BCrypt is a strong, adaptive hashing function designed for passwords.</p>
     * 
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configure authentication provider.
     * 
     * <p>Creates a DAO-based authentication provider that uses
     * the custom UserDetailsService and password encoder.</p>
     * 
     * @return Configured DaoAuthenticationProvider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Configure authentication manager.
     * 
     * <p>Exposes the AuthenticationManager bean for programmatic authentication.</p>
     * 
     * @param config Authentication configuration
     * @return AuthenticationManager instance
     * @throws Exception if configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
