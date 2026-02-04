package com.training.shopping_sys.controller;

import com.training.shopping_sys.dto.OrderProductDTO;
import com.training.shopping_sys.dto.ProductSearchResultDTO;
import com.training.shopping_sys.dto.StockValidationDTO;
import com.training.shopping_sys.entity.MstProduct;
import com.training.shopping_sys.entity.MstProductType;
import com.training.shopping_sys.repository.MstProductRepository;
import com.training.shopping_sys.repository.TrProductOrderRepository;
import com.training.shopping_sys.service.ImageService;
import com.training.shopping_sys.service.ProductService;
import com.training.shopping_sys.util.ImageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Product Controller.
 * 
 * <p>Handles HTTP requests related to product management including:
 * product listing, search, image upload, and stock validation.</p>
 * 
 * <p>This controller provides both web pages (returning Thymeleaf templates)
 * and RESTful API endpoints (returning JSON) for product operations.</p>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "APIs quản lý sản phẩm - Xem, tìm kiếm, upload ảnh")
public class ProductController {
    
    private final ProductService productService;
    private final MstProductRepository productRepository;
    private final TrProductOrderRepository orderRepository;
    private final ImageService imageService;
    private final ImageUtil imageUtil;

    @Value("${image.storage.path:uploads/products}")
    private String imageStoragePath;

    /**
     * Display product list page with search functionality.
     * 
     * <p>Shows all active product types and performs product search
     * based on keyword and/or product type. Supports pagination.</p>
     *
     * @param keyword Search keyword for product name (optional)
     * @param producttypeId Filter by product type ID (optional)
     * @param page Page number for pagination (default: 0)
     * @param model Spring MVC Model to pass data to view
     * @return Thymeleaf template name "product-list"
     */
    @GetMapping("/list")
    public String showProductList(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "producttypeId", required = false) Long producttypeId,
            @RequestParam(value = "page", required = false) Integer page,
            HttpSession session,
            Model model) {
        
        // Restore search state từ session nếu có (khi quay lại từ order page)
        if (keyword == null && producttypeId == null && page == null) {
            keyword = (String) session.getAttribute("searchKeyword");
            producttypeId = (Long) session.getAttribute("searchProductTypeId");
            Integer sessionPage = (Integer) session.getAttribute("searchPage");
            if (sessionPage != null) {
                page = sessionPage;
            }
        }
        
        // Default page to 0 if null or negative
        int pageNumber = (page != null && page >= 0) ? page : 0;
        
        // Get all active product types for dropdown
        List<MstProductType> productTypes = productService.getAllActiveProductTypes();
        model.addAttribute("productTypes", productTypes);
        
        // Perform search only if keyword or producttypeId is provided
        ProductSearchResultDTO searchResult = null;
        if ((keyword != null && !keyword.trim().isEmpty()) || producttypeId != null) {
            searchResult = productService.searchProducts(keyword, producttypeId, pageNumber);
        }
        
        model.addAttribute("searchResult", searchResult);
        
        // Restore product quantities từ session nếu có
        @SuppressWarnings("unchecked")
        Map<Long, Integer> productQuantities = (Map<Long, Integer>) session.getAttribute("productQuantities");
        if (productQuantities != null) {
            model.addAttribute("productQuantities", productQuantities);
        }
        
        return "product-list";
    }
    
    /**
     * Serve product images from database.
     * 
     * <p>Retrieves product image bytes from database and returns them
     * with appropriate content type and caching headers. Supports
     * JPEG, PNG, and other image formats.</p>
     * 
     * @param productId The ID of the product
     * @return ResponseEntity with image bytes and HTTP headers, or 404 if not found
     */
    @GetMapping("/image/{productId}")
    public ResponseEntity<byte[]> getProductImage(@PathVariable Long productId) {
        try {
            // Try to read from img/ folder first
            byte[] imageBytes = null;
            String contentType = "image/jpeg";

            Optional<MstProduct> productOpt = productRepository.findById(productId);
            if (productOpt.isPresent() && productOpt.get().getProductImg() != null) {
                imageBytes = productOpt.get().getProductImg();
                contentType = imageUtil.detectContentTypeFromBytes(imageBytes);
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
     * Validate stock availability for a product.
     * 
     * <p>Checks if the requested quantity is available in stock by
     * calculating: available stock = product amount - total ordered amount.
     * Returns validation result with availability status and message.</p>
     * 
     * @param productId The ID of the product to validate
     * @param quantity The requested quantity to check
     * @return ResponseEntity containing StockValidationDTO with validation result
     */
    @Operation(summary = "Kiểm tra tồn kho", description = "Kiểm tra số lượng tồn kho có sẵn của sản phẩm")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Kiểm tra thành công"),
        @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ")
    })
    @GetMapping("/validate-stock")
    @ResponseBody
    public ResponseEntity<StockValidationDTO> validateStock(
            @Parameter(description = "ID sản phẩm cần kiểm tra", required = true, example = "1")
            @RequestParam Long productId,
            @Parameter(description = "Số lượng muốn mua", required = true, example = "5")
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
     * Upload product image to database.
     * 
     * <p>Accepts multipart file upload (PNG/JPG) and stores the image
     * in the database as binary data. The image is compressed before storage
     * to optimize database size.</p>
     * 
     * @param productId ID of the product to update
     * @param file Multipart image file (PNG/JPG/JPEG)
     * @return ResponseEntity with success status and message
     */
    @Operation(
        summary = "Upload ảnh sản phẩm",
        description = "Upload file ảnh (PNG/JPG) cho sản phẩm. File sẽ được lưu vào database."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Upload thành công"),
        @ApiResponse(responseCode = "400", description = "File không hợp lệ hoặc product không tồn tại"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @PostMapping(value = "/upload-image/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadProductImage(
            @Parameter(description = "ID của sản phẩm cần upload ảnh", required = true, example = "1")
            @PathVariable Long productId,
            @Parameter(
                description = "File ảnh sản phẩm (PNG, JPG, JPEG)",
                required = true,
                content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestPart("file") MultipartFile file) {
        
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
     * Load images from file system to database.
     * 
     * <p>Bulk loads product images from the img/ folder following naming
     * convention: product_{productId}.jpg or product_{productId}.png.
     * Images are read from files and stored in database.</p>
     * 
     * @return ResponseEntity with count of successfully loaded images
     */
    @Operation(
        summary = "Load ảnh từ thư mục img",
        description = "Đọc tất cả ảnh từ thư mục img/ theo quy ước tên file product_{productId}.jpg và lưu vào database"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Load ảnh thành công")
    })
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
    
    /**
     * Chuyển sang trang đặt hàng.
     * 
     * <p>Xử lý request từ product list page, lấy danh sách sản phẩm có số lượng > 0,
     * lưu vào session và chuyển hướng sang trang order.</p>
     * 
     * <p>Session sẽ lưu:
     * - orderProducts: danh sách sản phẩm đã chọn
     * - searchKeyword: từ khóa tìm kiếm
     * - searchProductTypeId: loại sản phẩm đã chọn
     * - searchPage: trang hiện tại
     * - productQuantities: Map số lượng đã nhập cho từng sản phẩm</p>
     * 
     * @param quantities Map chứa quantity cho từng product (key: "quantity_{productId}")
     * @param keyword Search keyword từ form
     * @param producttypeId Product type ID từ form
     * @param page Page number từ form
     * @param session HTTP Session để lưu orderProducts và search state
     * @param redirectAttributes Attributes để truyền message
     * @return Redirect tới trang order
     */
    @PostMapping("/toOrder")
    public String toOrderPage(
            @RequestParam Map<String, String> quantities,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "producttypeId", required = false) Long producttypeId,
            @RequestParam(value = "page", required = false) Integer page,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        // Lấy danh sách sản phẩm có số lượng > 0
        List<OrderProductDTO> orderProducts = new ArrayList<>();
        
        for (Map.Entry<String, String> entry : quantities.entrySet()) {
            if (entry.getKey().startsWith("quantity_")) {
                String productIdStr = entry.getKey().replace("quantity_", "");
                String quantityStr = entry.getValue();
                
                if (quantityStr != null && !quantityStr.trim().isEmpty()) {
                    try {
                        Long productId = Long.parseLong(productIdStr);
                        int quantity = Integer.parseInt(quantityStr);
                        
                        if (quantity > 0) {
                            // Lấy thông tin sản phẩm từ DB
                            Optional<MstProduct> productOpt = productRepository.findById(productId);
                            
                            if (productOpt.isPresent()) {
                                MstProduct product = productOpt.get();
                                
                                OrderProductDTO orderProduct = OrderProductDTO.builder()
                                    .productId(productId)
                                    .productName(product.getProductName())
                                    .price(product.getProductPrice())
                                    .orderQuantity(quantity)
                                    .build();
                                
                                orderProducts.add(orderProduct);
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Skip invalid quantity
                        log.warn("Invalid quantity format: {}", quantityStr);
                    }
                }
            }
        }
        
        if (orderProducts.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Xin hãy chọn ít nhất một sản phẩm để đặt hàng");
            return "redirect:/products/list";
        }
        
        // Lưu orderProducts vào session
        session.setAttribute("orderProducts", orderProducts);
        
        // Lưu search state vào session để restore khi cancel
        session.setAttribute("searchKeyword", keyword);
        session.setAttribute("searchProductTypeId", producttypeId);
        session.setAttribute("searchPage", page != null ? page : 0);
        
        // Lưu tất cả quantities đã nhập vào session (bao gồm cả những sản phẩm có quantity = 0)
        Map<Long, Integer> productQuantities = new HashMap<>();
        for (Map.Entry<String, String> entry : quantities.entrySet()) {
            if (entry.getKey().startsWith("quantity_")) {
                String productIdStr = entry.getKey().replace("quantity_", "");
                String quantityStr = entry.getValue();
                
                if (quantityStr != null && !quantityStr.trim().isEmpty()) {
                    try {
                        Long productId = Long.parseLong(productIdStr);
                        int quantity = Integer.parseInt(quantityStr);
                        productQuantities.put(productId, quantity);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid quantity format: {}", quantityStr);
                    }
                }
            }
        }
        session.setAttribute("productQuantities", productQuantities);
        
        return "redirect:/order";
    }
}
