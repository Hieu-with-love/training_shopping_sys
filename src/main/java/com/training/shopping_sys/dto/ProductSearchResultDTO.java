package com.training.shopping_sys.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Product Search Result Data Transfer Object.
 * 
 * <p>Contains paginated product search results along with pagination
 * metadata and original search criteria. Used for displaying search
 * results with navigation controls.</p>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchResultDTO {
    /** List of products in current page. */
    private List<ProductSearchDTO> products;
    
    /** Current page number (0-based). */
    private int currentPage;
    
    /** Total number of pages. */
    private int totalPages;
    
    /** Total number of products matching search criteria. */
    private long totalElements;
    
    /** Flag indicating if there is a next page. */
    private boolean hasNext;
    
    /** Flag indicating if there is a previous page. */
    private boolean hasPrevious;
    
    /** Original search keyword. */
    private String keyword;
    
    /** Original product type filter. */
    private Long producttypeId;
}
