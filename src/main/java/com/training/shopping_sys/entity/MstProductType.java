package com.training.shopping_sys.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Product Type Master Entity.
 * 
 * <p>Represents a category or type of products in the shopping system.
 * Products are classified into different types for better organization
 * and filtering capabilities.</p>
 * 
 * <p>The status field indicates whether the product type is active ('0')
 * or deleted ('1').</p>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "mstproducttype")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MstProductType {
    
    /** Unique identifier for the product type. Auto-generated. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "producttype_id")
    private Long producttypeId;
    
    /** Name of the product type. Maximum length: 400 characters. */
    @Column(name = "producttype_name", length = 400)
    private String producttypeName;
    
    /** Status of the product type: '0' = active, '1' = deleted. */
    @Column(name = "status", length = 1)
    private String status;
}
