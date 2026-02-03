package com.training.shopping_sys.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Order Product Data Transfer Object.
 * 
 * <p>Đại diện cho thông tin sản phẩm trong đơn hàng, bao gồm ID, tên,
 * giá, số lượng đặt hàng và số lượng khả dụng trong kho.</p>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderProductDTO {
    
    /** ID của sản phẩm */
    private Long productId;
    
    /** Tên sản phẩm */
    private String productName;
    
    /** Giá sản phẩm */
    private Integer price;
    
    /** Số lượng đặt hàng */
    private Integer orderQuantity;
    
    /** Số lượng tồn kho có thể đặt hàng */
    private Integer availableQuantity;
}
