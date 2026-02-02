package com.training.shopping_sys.repository;

import com.training.shopping_sys.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User Repository.
 * 
 * <p>Data access interface for {@link User} entity.
 * Provides CRUD operations and authentication-related queries.</p>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by username.
     * 
     * <p>Used for authentication to retrieve user credentials and role.
     * Username is unique in the system.</p>
     * 
     * @param username Username to search for
     * @return Optional containing User if found, empty otherwise
     */
    Optional<User> findByUsername(String username);
}
