package com.training.shopping_sys.service;

import com.training.shopping_sys.entity.User;
import com.training.shopping_sys.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Custom User Details Service.
 * 
 * <p>Implementation of Spring Security's UserDetailsService interface.
 * Loads user-specific data for authentication and authorization.</p>
 * 
 * <p>Converts application User entity to Spring Security UserDetails
 * with appropriate authorities based on user role.</p>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    /**
     * Load user by username for authentication.
     * 
     * <p>Retrieves user from database and converts to Spring Security's
     * UserDetails object with granted authorities based on user role.</p>
     * 
     * @param username Username to search for
     * @return UserDetails object with user credentials and authorities
     * @throws UsernameNotFoundException if user not found in database
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(user.getRole())))
                .disabled(!user.isEnabled())
                .build();
    }
}
