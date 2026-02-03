package com.training.shopping_sys.service;

import com.training.shopping_sys.dto.OrderProductDTO;
import com.training.shopping_sys.dto.OrderRequestDTO;
import com.training.shopping_sys.entity.TrProductOrder;
import com.training.shopping_sys.entity.TrProductOrderKey;
import com.training.shopping_sys.repository.MstProductRepository;
import com.training.shopping_sys.repository.TrProductOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Order Service.
 * 
 * <p>Service xử lý logic nghiệp vụ cho việc đặt hàng, bao gồm:
 * - Tính toán số lượng sản phẩm khả dụng trong kho
 * - Validate thông tin đặt hàng
 * - Xử lý đơn đặt hàng</p>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final MstProductRepository productRepository;
    private final TrProductOrderRepository productOrderRepository;
    
    /**
     * Tính số lượng available cho sản phẩm.
     * 
     * <p>Với mỗi sản phẩm, tính số lượng khả dụng bằng cách lấy tổng số lượng
     * trong kho trừ đi số lượng đã đặt hàng.</p>
     * 
     * @param products Danh sách sản phẩm cần tính toán
     * @return Danh sách sản phẩm đã được cập nhật số lượng khả dụng
     */
    public List<OrderProductDTO> calculateAvailableQuantity(List<OrderProductDTO> products) {
        for (OrderProductDTO product : products) {
            // Lấy tổng số lượng sản phẩm trong kho
            Integer totalAmount = productRepository.findById(product.getProductId())
                    .map(p -> p.getProductAmount())
                    .orElse(0);
            
            // Lấy tổng số lượng đã đặt hàng
            Integer orderedAmount = productOrderRepository.getTotalOrderedAmount(product.getProductId());
            
            // Tính số lượng khả dụng
            product.setAvailableQuantity(totalAmount - orderedAmount);
        }
        return products;
    }
    
    /**
     * ⑤ Xử lý đặt hàng với validation.
     * 
     * <p>Thực hiện các validation sau:
     * 1. Validate người đặt hàng (bắt buộc)
     * 2. Validate địa chỉ giao hàng (bắt buộc)
     * 3. Validate ngày giao hàng (định dạng YYYY/MM/DD và >= ngày hiện tại)
     * 4. Validate số lượng đặt hàng không vượt quá số lượng tồn kho</p>
     * 
     * <p>Nếu validation thành công, tạo đơn hàng mới và lưu vào database.</p>
     * 
     * @param orderRequest Request đặt hàng từ form
     * @param userId ID của user đang đặt hàng
     * @return "SUCCESS" nếu thành công, "ERROR:message:focusField" nếu có lỗi
     */
    @Transactional
    public String processOrder(OrderRequestDTO orderRequest, String userId) {
        
        // Validate ① Người đặt hàng
        if (orderRequest.getCustomerName() == null || orderRequest.getCustomerName().trim().isEmpty()) {
            return "ERROR:Xin hãy nhập thông tin người đặt hàng:customerName";
        }
        
        // Validate ② Địa chỉ giao hàng
        if (orderRequest.getDeliveryAddress() == null || orderRequest.getDeliveryAddress().trim().isEmpty()) {
            return "ERROR:Xin hãy nhập thông tin địa chỉ giao hàng:deliveryAddress";
        }
        
        // Validate ③ Ngày giao hàng
        String deliveryDateStr = orderRequest.getDeliveryDate();
        LocalDate deliveryDate = null;
        
        if (deliveryDateStr != null && !deliveryDateStr.trim().isEmpty()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                deliveryDate = LocalDate.parse(deliveryDateStr, formatter);
                
                // Kiểm tra ngày >= ngày hiện tại
                if (deliveryDate.isBefore(LocalDate.now())) {
                    return "ERROR:Ngày giao hàng không hợp lệ, xin hãy nhập lại:deliveryDate";
                }
            } catch (DateTimeParseException e) {
                return "ERROR:Ngày giao hàng không hợp lệ, xin hãy nhập lại:deliveryDate";
            }
        }
        
        // Validate số lượng sản phẩm
        List<OrderProductDTO> products = orderRequest.getProducts();
        if (products == null || products.isEmpty()) {
            return "ERROR:Không có sản phẩm nào để đặt hàng:customerName";
        }
        
        List<String> invalidProducts = new ArrayList<>();
        int firstInvalidIndex = -1;
        OrderProductDTO firstInvalidProduct = null;
        
        for (int i = 0; i < products.size(); i++) {
            OrderProductDTO product = products.get(i);
            
            if (product.getOrderQuantity() != null && product.getOrderQuantity() > 0) {
                // Lấy tổng số lượng trong kho
                Integer totalAmount = productRepository.findById(product.getProductId())
                        .map(p -> p.getProductAmount())
                        .orElse(0);
                
                // Lấy số lượng đã đặt hàng
                Integer orderedAmount = productOrderRepository.getTotalOrderedAmount(product.getProductId());
                Integer availableQuantity = totalAmount - orderedAmount;
                
                if (product.getOrderQuantity() > availableQuantity) {
                    invalidProducts.add(String.format("Sản phẩm %s có số lượng tồn kho %d", 
                        product.getProductName(), availableQuantity));
                    
                    if (firstInvalidIndex == -1) {
                        firstInvalidIndex = i;
                        firstInvalidProduct = product;
                        firstInvalidProduct.setAvailableQuantity(availableQuantity);
                    }
                }
            }
        }
        
        // Nếu có sản phẩm không hợp lệ
        if (!invalidProducts.isEmpty()) {
            if (invalidProducts.size() == 1 && firstInvalidProduct != null) {
                return String.format("ERROR:Số lượng đặt hàng của sản phẩm %s không đủ trong kho. Xin hãy nhập số lượng <= %d:quantity_%d",
                    firstInvalidProduct.getProductName(), 
                    firstInvalidProduct.getAvailableQuantity(),
                    firstInvalidIndex);
            } else {
                StringBuilder message = new StringBuilder("Dưới đây các sản phẩm mà số lượng đặt hàng lớn hơn số lượng trong kho. Xin hãy nhập lại số lượng\\n");
                for (String invalidProduct : invalidProducts) {
                    message.append(invalidProduct).append("\\n");
                }
                return "ERROR:" + message.toString() + ":quantity_0";
            }
        }
        
        // Lấy order ID mới
        Long maxOrderId = productOrderRepository.findMaxOrderId().orElse(0L);
        Long newOrderId = maxOrderId + 1;
        
        // Insert data
        try {
            String deliveryDateFormatted = deliveryDate != null ? 
                deliveryDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) : null;
            
            LocalDateTime now = LocalDateTime.now();
            
            for (OrderProductDTO product : products) {
                if (product.getOrderQuantity() != null && product.getOrderQuantity() > 0) {
                    // Tạo composite key
                    TrProductOrderKey key = TrProductOrderKey.builder()
                        .orderId(newOrderId)
                        .customerName(orderRequest.getCustomerName())
                        .productId(product.getProductId())
                        .build();
                    
                    // Tạo order entity
                    TrProductOrder order = TrProductOrder.builder()
                        .id(key)
                        .orderProductAmount(product.getOrderQuantity())
                        .orderDeliveryAddress(orderRequest.getDeliveryAddress())
                        .orderDeliveryDate(deliveryDateFormatted)
                        .orderDate(now)
                        .build();
                    
                    productOrderRepository.save(order);
                }
            }
            
            return "SUCCESS";
            
        } catch (Exception e) {
            throw new RuntimeException("Database error", e);
        }
    }
}
