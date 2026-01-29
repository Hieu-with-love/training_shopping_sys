package com.training.shopping_sys.controller;

import com.training.shopping_sys.dto.ProductSearchResultDTO;
import com.training.shopping_sys.entity.MstProduct;
import com.training.shopping_sys.entity.MstProductType;
import com.training.shopping_sys.repository.MstProductRepository;
import com.training.shopping_sys.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    private final MstProductRepository productRepository;
    
    @GetMapping("/list")
    public String showProductList(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "producttypeId", required = false) Long producttypeId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        
        // Get all active product types for dropdown
        List<MstProductType> productTypes = productService.getAllActiveProductTypes();
        model.addAttribute("productTypes", productTypes);
        
        // Perform search only if keyword or producttypeId is provided
        ProductSearchResultDTO searchResult = null;
        if ((keyword != null && !keyword.trim().isEmpty()) || producttypeId != null) {
            searchResult = productService.searchProducts(keyword, producttypeId, page);
        }
        
        model.addAttribute("searchResult", searchResult);
        
        return "product-list";
    }
    
    /**
     * Endpoint to serve product images as byte array
     * @param productId The ID of the product
     * @return ResponseEntity with image bytes and appropriate content type
     */
    @GetMapping("/image/{productId}")
    public ResponseEntity<byte[]> getProductImage(@PathVariable Long productId) {
        Optional<MstProduct> productOpt = productRepository.findById(productId);
        
        if (productOpt.isPresent() && productOpt.get().getProductImg() != null) {
            byte[] imageBytes = productOpt.get().getProductImg();
            
            HttpHeaders headers = new HttpHeaders();
            // Set content type based on image format (default to JPEG)
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentLength(imageBytes.length);
            
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        }
        
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
