package com.training.shopping_sys.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Entity.
 * 
 * <p>Represents a user account in the shopping system. This entity is used
 * for authentication and authorization purposes. Each user has a unique
 * username, encrypted password, and assigned role.</p>
 * 
 * <p>Supported roles include ROLE_USER and ROLE_ADMIN. The enabled flag
 * indicates whether the user account is active.</p>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    /** Unique identifier for the user. Auto-generated. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** Username for login. Must be unique and cannot be null. */
    @Column(unique = true, nullable = false)
    private String username;
    
    /** Encrypted password. Cannot be null. */
    @Column(nullable = false)
    private String password;
    
    /** User role for authorization. Values: ROLE_USER, ROLE_ADMIN. Cannot be null. */
    @Column(nullable = false)
    private String role;
    
    /** Flag indicating whether the user account is enabled. Default is true. */
    private boolean enabled = true;
}
