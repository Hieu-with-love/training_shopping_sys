package com.training.shopping_sys.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Product Search Data Transfer Object.
 * 
 * <p>Represents a single product in search results. Contains product
 * information including image URL, type, and total ordered amount for
 * popularity sorting.</p>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchDTO {
    /** Product identifier. */
    private Long productId;
    
    /** Product name. */
    private String productName;
    
    /** Product description. */
    private String productDescription;
    
    /** Flag indicating if product has an image. */
    private boolean hasImage;
    
    /** URL to access product image (e.g., /products/image/1). */
    private String imageUrl;
    
    /** Product type identifier. */
    private Long producttypeId;
    
    /** Product type name. */
    private String producttypeName;
    
    /** Total quantity ordered across all orders (for popularity). */
    private Long totalOrdered;
}
