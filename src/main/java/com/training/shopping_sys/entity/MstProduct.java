package com.training.shopping_sys.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Product Master Entity.
 * 
 * <p>Represents a product in the shopping system's product catalog.
 * This entity contains all information about a product including its name,
 * description, image, type, stock amount, and status.</p>
 * 
 * <p>The product image is stored as binary data (bytea) in the database.
 * The status field indicates whether the product is active ('0') or deleted ('1').</p>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "mstproduct")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MstProduct {
    
    /** Unique identifier for the product. Auto-generated. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;
    
    /** Name of the product. Maximum length: 400 characters. */
    @Column(name = "product_name", length = 400)
    private String productName;
    
    /** Detailed description of the product. Maximum length: 400 characters. */
    @Column(name = "product_description", length = 400)
    private String productDescription;
    
    /** Product image stored as binary data. */
    @Column(name = "product_img", columnDefinition = "bytea")
    private byte[] productImg;
    
    /** Foreign key reference to the product type. */
    @Column(name = "producttype_id")
    private Long producttypeId;
    
    /** Associated product type entity. Many-to-one relationship. */
    @ManyToOne
    @JoinColumn(name = "producttype_id", insertable = false, updatable = false)
    private MstProductType productType;
    
    /** Available stock amount for the product. */
    @Column(name = "product_amount")
    private Integer productAmount;
    
    /** Status of the product: '0' = active, '1' = deleted. */
    @Column(name = "status", length = 1)
    private String status;
}
