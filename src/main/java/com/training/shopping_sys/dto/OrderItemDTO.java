package com.training.shopping_sys.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Order Item Data Transfer Object.
 * 
 * <p>Represents an item in a customer's order, containing product details,
 * quantity, and stock availability information. Used for displaying order
 * confirmation details before final submission.</p>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    /** Product identifier. */
    private Long productId;
    
    /** Product name. */
    private String productName;
    
    /** Product description. */
    private String productDescription;
    
    /** Quantity to order. */
    private Integer quantity;
    
    /** Available stock for this product. */
    private Integer availableStock;
    
    /** Product image URL or data. */
    private String productImage;
    
    /** Product type/category name. */
    private String producttypeName;
}
