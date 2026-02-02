package com.training.shopping_sys.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Stock Validation Data Transfer Object.
 * 
 * <p>Contains the result of stock availability validation for a product.
 * Used in AJAX responses to provide real-time feedback on order quantities.</p>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockValidationDTO {
    /** Flag indicating if requested quantity is available. */
    private boolean valid;
    
    /** Validation message (error or success). */
    private String message;
    
    /** Available stock quantity. */
    private Integer availableStock;
    
    /** Product name being validated. */
    private String productName;
}
