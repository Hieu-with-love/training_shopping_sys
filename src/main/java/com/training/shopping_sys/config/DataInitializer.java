package com.training.shopping_sys.config;

import com.training.shopping_sys.entity.User;
import com.training.shopping_sys.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Data Initializer.
 * 
 * <p>Initializes default user accounts on application startup.
 * Runs once when the application starts and creates test users
 * if they don't already exist in the database.</p>
 * 
 * <p>Default users created:
 * - admin/admin123 (ROLE_ADMIN)
 * - user/user123 (ROLE_USER)
 * </p>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Initialize default user accounts.
     * 
     * <p>Executed on application startup. Creates admin and regular user
     * accounts if they don't exist. Passwords are encrypted with BCrypt.</p>
     * 
     * @param args Command-line arguments (unused)
     * @throws Exception if initialization fails
     */
    @Override
    public void run(String... args) throws Exception {
        // Tạo user test nếu chưa tồn tại
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ROLE_ADMIN");
            admin.setEnabled(true);
            userRepository.save(admin);
            System.out.println("✓ Created admin user: admin/admin123");
        }
        
        if (userRepository.findByUsername("user").isEmpty()) {
            User user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRole("ROLE_USER");
            user.setEnabled(true);
            userRepository.save(user);
            System.out.println("✓ Created regular user: user/user123");
        }
    }
}
