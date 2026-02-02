package com.training.shopping_sys.dto;

/**
 * Projection interface for product search results.
 * 
 * <p>Used by Spring Data JPA to efficiently fetch only required fields
 * instead of loading full entities. Improves query performance by reducing
 * data transfer from database.</p>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
public interface ProductSearchProjection {
    
    /**
     * Get product ID.
     * @return Product ID
     */
    Long getProductId();
    
    /**
     * Get product name.
     * @return Product name
     */
    String getProductName();
    
    /**
     * Get product description.
     * @return Product description
     */
    String getProductDescription();
    
    /**
     * Get product image binary data.
     * @return Product image as byte array
     */
    byte[] getProductImg();
    
    /**
     * Get product type ID.
     * @return Product type ID
     */
    Long getProducttypeId();
    
    /**
     * Get product type name.
     * @return Product type name
     */
    String getProducttypeName();
    
    /**
     * Get total ordered amount.
     * @return Total ordered quantity across all orders
     */
    Long getTotalOrdered();
}
