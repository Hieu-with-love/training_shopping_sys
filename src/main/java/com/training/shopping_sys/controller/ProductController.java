package com.training.shopping_sys.controller;

import com.training.shopping_sys.dto.ProductSearchResultDTO;
import com.training.shopping_sys.dto.StockValidationDTO;
import com.training.shopping_sys.entity.MstProduct;
import com.training.shopping_sys.entity.MstProductType;
import com.training.shopping_sys.repository.MstProductRepository;
import com.training.shopping_sys.repository.TrProductOrderRepository;
import com.training.shopping_sys.service.ImageService;
import com.training.shopping_sys.service.ProductService;
import com.training.shopping_sys.util.ImageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    private final MstProductRepository productRepository;
    private final TrProductOrderRepository orderRepository;
    private final ImageService imageService;
    private final ImageUtil imageUtil;
    
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
     * Endpoint to serve product images
     * Ưu tiên đọc từ file img/, nếu không có thì đọc từ DB
     * @param productId The ID of the product
     * @return ResponseEntity with image bytes and appropriate content type
     */
    @GetMapping("/image/{productId}")
    public ResponseEntity<byte[]> getProductImage(@PathVariable Long productId) {
        try {
            // Try to read from img/ folder first
            byte[] imageBytes = null;
            String contentType = "image/jpeg";
            
            // Try jpg first
            Path jpgPath = Paths.get("img/product_" + productId + ".jpg");
            if (Files.exists(jpgPath)) {
                imageBytes = Files.readAllBytes(jpgPath);
                contentType = "image/jpeg";
            } else {
                // Try png
                Path pngPath = Paths.get("img/product_" + productId + ".png");
                if (Files.exists(pngPath)) {
                    imageBytes = Files.readAllBytes(pngPath);
                    contentType = "image/png";
                }
            }
            
            // If not found in files, try database
            if (imageBytes == null) {
                Optional<MstProduct> productOpt = productRepository.findById(productId);
                if (productOpt.isPresent() && productOpt.get().getProductImg() != null) {
                    imageBytes = productOpt.get().getProductImg();
                    contentType = imageUtil.detectContentTypeFromBytes(imageBytes);
                }
            }
            
            if (imageBytes != null) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(contentType));
                headers.setContentLength(imageBytes.length);
                headers.setCacheControl("max-age=3600");
                
                return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
            }
            
        } catch (Exception e) {
            log.error("Error reading image for product {}", productId, e);
        }
        
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    
    /**
     * API endpoint to validate stock availability
     * @param productId The ID of the product
     * @param quantity The requested quantity
     * @return StockValidationDTO with validation result
     */
    @GetMapping("/validate-stock")
    @ResponseBody
    public ResponseEntity<StockValidationDTO> validateStock(
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        
        Optional<MstProduct> productOpt = productRepository.findById(productId);
        
        if (productOpt.isEmpty()) {
            StockValidationDTO dto = new StockValidationDTO();
            dto.setValid(false);
            dto.setMessage("Sản phẩm không tồn tại");
            return ResponseEntity.ok(dto);
        }
        
        MstProduct product = productOpt.get();
        Integer totalOrdered = orderRepository.getTotalOrderedAmount(productId);
        Integer availableStock = (product.getProductAmount() != null ? product.getProductAmount() : 0) - totalOrdered;
        
        StockValidationDTO dto = new StockValidationDTO();
        dto.setProductName(product.getProductName());
        dto.setAvailableStock(availableStock);
        
        if (quantity > availableStock) {
            dto.setValid(false);
            dto.setMessage(String.format("Số lượng đặt hàng của sản phẩm %s không đủ trong kho. Xin hãy nhập số lượng <= %d", 
                product.getProductName(), availableStock));
        } else {
            dto.setValid(true);
            dto.setMessage("Số lượng hợp lệ");
        }
        
        return ResponseEntity.ok(dto);
    }
    
    /**
     * API endpoint để upload ảnh cho sản phẩm
     * @param productId ID của sản phẩm
     * @param file File ảnh (png/jpg)
     * @return Response với status và message
     */
    @PostMapping("/upload-image/{productId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadProductImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file) {
        
        Map<String, Object> response = new HashMap<>();
        
        boolean success = imageService.updateProductImage(productId, file);
        
        if (success) {
            response.put("success", true);
            response.put("message", "Cập nhật ảnh thành công cho sản phẩm " + productId);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Không thể cập nhật ảnh. Vui lòng kiểm tra file và product ID");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * API endpoint để load ảnh từ thư mục img cho tất cả sản phẩm
     * Quy ước tên file: product_{productId}.jpg hoặc product_{productId}.png
     * @return Response với số lượng ảnh đã load
     */
    @PostMapping("/load-images")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> loadImagesFromFolder() {
        Map<String, Object> response = new HashMap<>();
        
        int count = imageService.loadImagesFromImgFolder();
        
        response.put("success", true);
        response.put("message", "Đã load " + count + " ảnh từ thư mục img");
        response.put("count", count);
        
        return ResponseEntity.ok(response);
    }
}
