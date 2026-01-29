package com.training.shopping_sys.controller;

import com.training.shopping_sys.dto.ProductSearchResultDTO;
import com.training.shopping_sys.entity.MstProductType;
import com.training.shopping_sys.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    
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
}
