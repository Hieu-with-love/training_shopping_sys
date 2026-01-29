package com.training.shopping_sys.service;

import com.training.shopping_sys.dto.ProductSearchDTO;
import com.training.shopping_sys.dto.ProductSearchResultDTO;
import com.training.shopping_sys.entity.MstProductType;
import com.training.shopping_sys.repository.MstProductRepository;
import com.training.shopping_sys.repository.MstProductTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final MstProductRepository productRepository;
    private final MstProductTypeRepository productTypeRepository;
    
    private static final int PAGE_SIZE = 5;
    
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
            byte[] imageBytes = (byte[]) result[3];
            dto.setHasImage(imageBytes != null && imageBytes.length > 0);
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
    
    public List<MstProductType> getAllActiveProductTypes() {
        return productTypeRepository.findAllActive();
    }
}
