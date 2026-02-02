package com.training.shopping_sys.service;

import com.training.shopping_sys.dto.ProductSearchDTO;
import com.training.shopping_sys.dto.ProductSearchResultDTO;
import com.training.shopping_sys.entity.MstProductType;
import com.training.shopping_sys.repository.MstProductRepository;
import com.training.shopping_sys.repository.MstProductTypeRepository;
import com.training.shopping_sys.util.ImageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Product Service.
 * 
 * <p>Business logic layer for product-related operations including:
 * product search with pagination, filtering by keyword and product type,
 * and retrieving active product types.</p>
 * 
 * <p>Implements pagination with configurable page size and handles
 * image URL generation for product images stored in database.</p>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final MstProductRepository productRepository;
    private final MstProductTypeRepository productTypeRepository;
    private final ImageUtil imageUtil;
    
    /** Number of products to display per page. */
    private static final int PAGE_SIZE = 5;
    
    /**
     * Search products with pagination.
     * 
     * <p>Searches products by keyword (product name) and/or product type.
     * Results are paginated with configurable page size. Keyword is
     * truncated to 400 characters if exceeds limit.</p>
     * 
     * <p>For each product, calculates total ordered amount and generates
     * image URL if image exists in database.</p>
     * 
     * @param keyword Search keyword for product name (optional, max 400 chars)
     * @param producttypeId Filter by product type ID (optional)
     * @param page Page number (0-based)
     * @return ProductSearchResultDTO containing paginated results and metadata
     */
    public ProductSearchResultDTO searchProducts(String keyword, Long producttypeId, int page) {
        // Validate keyword length
        if (keyword != null && keyword.length() > 400) {
            keyword = keyword.substring(0, 400);
        }
        
        // Get all matching products
        List<Object[]> results = productRepository.searchProducts(keyword, producttypeId);
        
        // Convert to DTOs
        List<ProductSearchDTO> allProducts = new ArrayList<>();
        for (Object[] result : results) {
            ProductSearchDTO dto = new ProductSearchDTO();
            dto.setProductId(((Number) result[0]).longValue());
            dto.setProductName((String) result[1]);
            dto.setProductDescription((String) result[2]);
            
            // result[3]: product_img (byte[])
            byte[] imageBytes = (byte[]) result[3];
            if (imageBytes != null && imageBytes.length > 0) {
                dto.setHasImage(true);
                // Set URL to serve image from /products/image/{id} endpoint
                dto.setImageUrl("/products/image/" + dto.getProductId());
            } else {
                dto.setHasImage(false);
                dto.setImageUrl(null);
            }
            
            dto.setProducttypeId(((Number) result[4]).longValue());
            dto.setProducttypeName((String) result[5]);
            dto.setTotalOrdered(((Number) result[6]).longValue());
            allProducts.add(dto);
        }
        
        // Pagination
        int totalElements = allProducts.size();
        int totalPages = (int) Math.ceil((double) totalElements / PAGE_SIZE);
        
        // Adjust page if out of bounds
        if (page < 0) page = 0;
        if (page >= totalPages && totalPages > 0) page = totalPages - 1;
        
        // Get products for current page
        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, totalElements);
        List<ProductSearchDTO> pageProducts = (start < totalElements) 
            ? allProducts.subList(start, end) 
            : new ArrayList<>();
        
        // Build result
        ProductSearchResultDTO result = new ProductSearchResultDTO();
        result.setProducts(pageProducts);
        result.setCurrentPage(page);
        result.setTotalPages(totalPages);
        result.setTotalElements(totalElements);
        result.setHasNext(page < totalPages - 1);
        result.setHasPrevious(page > 0);
        result.setKeyword(keyword);
        result.setProducttypeId(producttypeId);
        
        return result;
    }
    
    /**
     * Get all active product types.
     * 
     * <p>Retrieves all product types with status = '0' (active).
     * Used for populating filter dropdowns in product search.</p>
     * 
     * @return List of active product types
     */
    public List<MstProductType> getAllActiveProductTypes() {
        return productTypeRepository.findAllActive();
    }
}
