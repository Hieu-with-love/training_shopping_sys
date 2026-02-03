package com.training.shopping_sys.dto;

import lombok.Data;
import java.util.List;

/**
 * Order Request Data Transfer Object.
 * 
 * <p>Đại diện cho request đặt hàng từ form, bao gồm thông tin khách hàng,
 * địa chỉ giao hàng, ngày giao hàng và danh sách sản phẩm đặt hàng.</p>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
@Data
public class OrderRequestDTO {
    
    /** Tên người đặt hàng (max 200 ký tự) */
    private String customerName;
    
    /** Địa chỉ giao hàng (max 400 ký tự) */
    private String deliveryAddress;
    
    /** Ngày giao hàng (định dạng YYYY/MM/DD) */
    private String deliveryDate;
    
    /** Danh sách sản phẩm đặt hàng */
    private List<OrderProductDTO> products;
}
