package com.training.shopping_sys.service;

import com.training.shopping_sys.dto.ProductSearchDTO;
import com.training.shopping_sys.dto.ProductSearchProjection;
import com.training.shopping_sys.dto.ProductSearchResultDTO;
import com.training.shopping_sys.entity.MstProductType;
import com.training.shopping_sys.repository.MstProductRepository;
import com.training.shopping_sys.repository.MstProductTypeRepository;
import com.training.shopping_sys.util.ImageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Product Service.
 * 
 * <p>Business logic layer for product-related operations including:
 * product search with pagination, filtering by keyword and product type,
 * and retrieving active product types.</p>
 * 
 * <p>Implements pagination using Spring Data JPA Pageable for efficient
 * database queries and automatic pagination handling.</p>
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
     * Search products with pagination using JPA.
     * 
     * <p>Searches products by keyword (product name) and/or product type.
     * Results are paginated using Spring Data JPA Pageable. Keyword is
     * truncated to 400 characters if exceeds limit.</p>
     * 
     * <p>For each product, total ordered amount is calculated by the database
     * and image URL is generated if image exists.</p>
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
        
        // Adjust page if negative
        if (page < 0) page = 0;
        
        // Create pageable - note: sorting is handled in the query itself
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        
        // Execute search with pagination - JPA handles everything
        Page<ProductSearchProjection> productPage = productRepository.searchProducts(
            keyword, 
            producttypeId, 
            pageable
        );
        
        // Convert projections to DTOs
        List<ProductSearchDTO> pageProducts = productPage.getContent().stream()
            .map(projection -> {
                ProductSearchDTO dto = new ProductSearchDTO();
                dto.setProductId(projection.getProductId());
                dto.setProductName(projection.getProductName());
                dto.setProductDescription(projection.getProductDescription());
                
                byte[] imageBytes = projection.getProductImg();
                if (imageBytes != null && imageBytes.length > 0) {
                    dto.setHasImage(true);
                    dto.setImageUrl("/products/image/" + projection.getProductId());
                } else {
                    dto.setHasImage(false);
                    dto.setImageUrl(null);
                }
                
                dto.setProducttypeId(projection.getProducttypeId());
                dto.setProducttypeName(projection.getProducttypeName());
                dto.setTotalOrdered(projection.getTotalOrdered());
                
                return dto;
            })
            .toList();
        
        // Build result from Page object
        ProductSearchResultDTO result = new ProductSearchResultDTO();
        result.setProducts(pageProducts);
        result.setCurrentPage(productPage.getNumber());
        result.setTotalPages(productPage.getTotalPages());
        result.setTotalElements((int) productPage.getTotalElements());
        result.setHasNext(productPage.hasNext());
        result.setHasPrevious(productPage.hasPrevious());
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
