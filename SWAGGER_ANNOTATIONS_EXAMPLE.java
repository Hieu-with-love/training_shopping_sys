package com.training.shopping_sys.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * VÍ DỤ: Cách thêm Swagger annotations vào Controller
 * 
 * Copy các annotation này vào các Controller hiện tại để có documentation đẹp hơn
 */

@Tag(
    name = "Product Management", 
    description = "APIs để quản lý sản phẩm - Xem, tìm kiếm, và quản lý tồn kho"
)
@RestController
@RequestMapping("/api/products")
public class ProductControllerSwaggerExample {

    @Operation(
        summary = "Lấy danh sách tất cả sản phẩm",
        description = "Trả về danh sách đầy đủ các sản phẩm trong hệ thống kèm thông tin chi tiết"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lấy danh sách thành công",
            content = @Content(schema = @Schema(implementation = ProductListResponse.class))
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Chưa xác thực"
        )
    })
    @GetMapping("/list")
    public ResponseEntity<List<Product>> getAllProducts() {
        // Implementation
        return null;
    }

    @Operation(
        summary = "Tìm kiếm sản phẩm",
        description = "Tìm kiếm sản phẩm theo tên hoặc loại sản phẩm"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tìm kiếm thành công"),
        @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ")
    })
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(
        @Parameter(description = "Tên sản phẩm cần tìm (tìm kiếm gần đúng)", example = "iPhone")
        @RequestParam(required = false) String productName,
        
        @Parameter(description = "ID loại sản phẩm", example = "1")
        @RequestParam(required = false) Long productTypeId
    ) {
        // Implementation
        return null;
    }

    @Operation(
        summary = "Lấy chi tiết sản phẩm",
        description = "Lấy thông tin chi tiết của một sản phẩm theo ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Thành công"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(
        @Parameter(description = "ID của sản phẩm", required = true, example = "1")
        @PathVariable Long id
    ) {
        // Implementation
        return null;
    }

    @Operation(
        summary = "Kiểm tra tồn kho",
        description = "Kiểm tra số lượng tồn kho có sẵn của sản phẩm"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Kiểm tra thành công",
            content = @Content(schema = @Schema(implementation = StockValidationDTO.class))
        )
    })
    @GetMapping("/{productId}/stock")
    public ResponseEntity<StockValidationDTO> checkStock(
        @Parameter(description = "ID sản phẩm cần kiểm tra", required = true)
        @PathVariable Long productId,
        
        @Parameter(description = "Số lượng muốn mua", required = true, example = "5")
        @RequestParam Integer quantity
    ) {
        // Implementation
        return null;
    }

    // Các method khác...
}


/**
 * VÍ DỤ: Order Controller với Swagger annotations
 */
@Tag(
    name = "Order Management", 
    description = "APIs để quản lý đơn hàng - Đặt hàng và xem lịch sử"
)
@RestController
@RequestMapping("/api/orders")
class OrderControllerSwaggerExample {

    @Operation(
        summary = "Đặt hàng sản phẩm",
        description = "Tạo đơn hàng mới cho sản phẩm. Yêu cầu xác thực người dùng."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Đặt hàng thành công"),
        @ApiResponse(responseCode = "400", description = "Số lượng không hợp lệ hoặc vượt quá tồn kho"),
        @ApiResponse(responseCode = "404", description = "Sản phẩm không tồn tại"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @PostMapping("/place")
    public ResponseEntity<?> placeOrder(
        @Parameter(description = "ID sản phẩm muốn mua", required = true, example = "1")
        @RequestParam Long productId,
        
        @Parameter(description = "Số lượng muốn mua", required = true, example = "2")
        @RequestParam Integer quantity
    ) {
        // Implementation
        return null;
    }

    @Operation(
        summary = "Xem lịch sử đơn hàng",
        description = "Lấy danh sách tất cả đơn hàng của người dùng hiện tại"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping("/history")
    public ResponseEntity<List<Order>> getOrderHistory() {
        // Implementation
        return null;
    }
}


/**
 * HƯỚNG DẪN SỬ DỤNG:
 * 
 * 1. Thêm các import sau vào đầu Controller:
 *    import io.swagger.v3.oas.annotations.Operation;
 *    import io.swagger.v3.oas.annotations.Parameter;
 *    import io.swagger.v3.oas.annotations.responses.ApiResponse;
 *    import io.swagger.v3.oas.annotations.responses.ApiResponses;
 *    import io.swagger.v3.oas.annotations.tags.Tag;
 * 
 * 2. Thêm @Tag annotation vào class level của Controller
 * 
 * 3. Thêm @Operation, @ApiResponses vào mỗi method
 * 
 * 4. Thêm @Parameter vào các tham số của method
 * 
 * 5. Sau khi thêm, restart application và truy cập:
 *    http://localhost:8080/swagger-ui.html
 * 
 * LƯU Ý:
 * - Chỉ thêm annotations, KHÔNG thay đổi logic code
 * - Giữ nguyên cấu trúc Controller hiện tại
 * - Annotations chỉ để generate documentation, không ảnh hưởng runtime
 */
