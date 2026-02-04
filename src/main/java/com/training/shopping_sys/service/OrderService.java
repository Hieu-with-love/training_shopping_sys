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
     * ⑤ Xử lý đặt hàng với validation nghiệp vụ.
     * 
     * <p>Service Layer: Validate nghiệp vụ + Insert Data
     * 
     * Thực hiện validate theo thứ tự:
     * 1. ① Validate người đặt hàng (bắt buộc, max 200 ký tự)
     * 2. ② Validate địa chỉ giao hàng (bắt buộc, max 400 ký tự)
     * 3. ③ Validate ngày giao hàng:
     *    - Định dạng YYYY/MM/DD
     *    - Ngày hợp lệ
     *    - Ngày giao hàng >= ngày hiện tại
     * 4. Validate số lượng đặt hàng của từng sản phẩm:
     *    - Số lượng đặt <= (product_amount - sum(order_product_amount))
     *    - Nếu 1 sản phẩm lỗi: hiển thị message cho sản phẩm đó, focus vào quantity của sản phẩm
     *    - Nếu nhiều sản phẩm lỗi: hiển thị danh sách tất cả sản phẩm lỗi, focus vào quantity của sản phẩm đầu tiên
     * 
     * Nếu validate thành công:
     * - Lấy order_id mới = max(order_id) + 1
     * - Insert từng sản phẩm vào trproductorder
     * - Transaction được quản lý bởi @Transactional (auto commit/rollback)
     * </p>
     * 
     * @param orderRequest Request đặt hàng từ form
     * @param userId ID của user đang đặt hàng
     * @return "SUCCESS" nếu thành công, "ERROR:message:focusField" nếu có lỗi
     * @throws RuntimeException nếu có lỗi database (sẽ trigger rollback)
     */
    @Transactional
    public String processOrder(OrderRequestDTO orderRequest, String userId) {
        
        // Validate ① Người đặt hàng - bắt buộc nhập
        if (orderRequest.getCustomerName() == null || orderRequest.getCustomerName().trim().isEmpty()) {
            return "ERROR:Xin hãy nhập thông tin người đặt hàng:customerName";
        }
        
        // Validate ② Địa chỉ giao hàng - bắt buộc nhập
        if (orderRequest.getDeliveryAddress() == null || orderRequest.getDeliveryAddress().trim().isEmpty()) {
            return "ERROR:Xin hãy nhập thông tin địa chỉ giao hàng:deliveryAddress";
        }

        // Validate ③ Ngày giao hàng - bắt buộc nhập
        if (orderRequest.getDeliveryDate() == null || orderRequest.getDeliveryDate().trim().isEmpty()) {
            return "ERROR:Xin hãy nhập thông tin ngày giao hàng:deliveryDate";
        }

        
        // Validate ③ Ngày giao hàng
        String deliveryDateStr = orderRequest.getDeliveryDate();
        LocalDate deliveryDate = null;
        
        if (deliveryDateStr != null && !deliveryDateStr.trim().isEmpty()) {
            try {
                // Kiểm tra định dạng YYYY/MM/DD
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                deliveryDate = LocalDate.parse(deliveryDateStr, formatter);
                
                // Kiểm tra ngày giao hàng >= ngày hiện tại
                if (deliveryDate.isBefore(LocalDate.now())) {
                    return "ERROR:Ngày giao hàng không hợp lệ, xin hãy nhập lại:deliveryDate";
                }
            } catch (DateTimeParseException e) {
                // Định dạng không hợp lệ hoặc ngày không hợp lệ
                return "ERROR:Ngày giao hàng không hợp lệ, xin hãy nhập lại:deliveryDate";
            }
        }
        
        // Validate danh sách sản phẩm không rỗng
        List<OrderProductDTO> products = orderRequest.getProducts();
        if (products == null || products.isEmpty()) {
            return "ERROR:Không có sản phẩm nào để đặt hàng:customerName";
        }
        
        // Validate số lượng đặt hàng của từng sản phẩm
        List<String> invalidProducts = new ArrayList<>();
        int firstInvalidIndex = -1;
        OrderProductDTO firstInvalidProduct = null;
        
        for (int i = 0; i < products.size(); i++) {
            OrderProductDTO product = products.get(i);
            
            // Chỉ validate sản phẩm có số lượng đặt hàng > 0
            if (product.getOrderQuantity() != null && product.getOrderQuantity() > 0) {
                // Lấy số lượng tồn kho từ mstproduct
                Integer totalAmount = productRepository.findById(product.getProductId())
                        .map(p -> p.getProductAmount())
                        .orElse(0);
                
                // Lấy tổng số lượng đã đặt hàng từ trproductorder
                Integer orderedAmount = productOrderRepository.getTotalOrderedAmount(product.getProductId());
                
                // Tính số lượng khả dụng = tồn kho - đã đặt
                Integer availableQuantity = totalAmount - orderedAmount;
                
                // Kiểm tra số lượng đặt hàng > số lượng khả dụng
                if (product.getOrderQuantity() > availableQuantity) {
                    invalidProducts.add(String.format("Sản phẩm %s có số lượng tồn kho %d", 
                        product.getProductName(), availableQuantity));
                    
                    // Lưu sản phẩm lỗi đầu tiên để focus
                    if (firstInvalidIndex == -1) {
                        firstInvalidIndex = i;
                        firstInvalidProduct = product;
                        firstInvalidProduct.setAvailableQuantity(availableQuantity);
                    }
                }
            }
        }
        
        // Nếu có sản phẩm không hợp lệ về số lượng
        if (!invalidProducts.isEmpty()) {
            if (invalidProducts.size() == 1 && firstInvalidProduct != null) {
                // Chỉ có 1 sản phẩm lỗi
                return String.format("ERROR:Số lượng đặt hàng của sản phẩm %s không đủ trong kho. Xin hãy nhập số lượng <= %d:quantity_%d",
                    firstInvalidProduct.getProductName(), 
                    firstInvalidProduct.getAvailableQuantity(),
                    firstInvalidIndex);
            } else {
                // Có nhiều sản phẩm lỗi - hiển thị danh sách
                StringBuilder message = new StringBuilder("Dưới đây các sản phẩm mà số lượng đặt hàng lớn hơn số lượng trong kho. Xin hãy nhập lại số lượng\\n");
                for (String invalidProduct : invalidProducts) {
                    message.append(invalidProduct).append("\\n");
                }
                return "ERROR:" + message.toString() + ":quantity_" + firstInvalidIndex;
            }
        }
        
        // Validation thành công - bắt đầu xử lý insert
        
        // Lấy order ID mới = max(order_id) + 1
        Long maxOrderId = productOrderRepository.findMaxOrderId().orElse(0L);
        Long newOrderId = maxOrderId + 1;
        
        // Insert data cho từng sản phẩm
        try {
            // Format ngày giao hàng theo YYYYMMDD
            String deliveryDateFormatted = deliveryDate != null ? 
                deliveryDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) : null;
            
            LocalDateTime now = LocalDateTime.now();
            
            // Insert từng sản phẩm có số lượng đặt hàng > 0
            for (OrderProductDTO product : products) {
                if (product.getOrderQuantity() != null && product.getOrderQuantity() > 0) {
                    
                    // Tạo composite key
                    TrProductOrderKey key = TrProductOrderKey.builder()
                        .orderId(newOrderId)                        // order_id mới
                        .customerName(orderRequest.getCustomerName()) // từ input ①
                        .productId(product.getProductId())          // id sản phẩm
                        .build();
                    
                    // Tạo order entity
                    TrProductOrder order = TrProductOrder.builder()
                        .id(key)
                        .orderProductAmount(product.getOrderQuantity())           // số lượng đặt hàng
                        .orderDeliveryAddress(orderRequest.getDeliveryAddress())  // từ input ②
                        .orderDeliveryDate(deliveryDateFormatted)                 // từ input ③, format YYYYMMDD
                        .orderDate(now)                                           // CURRENT_TIMESTAMP
                        .build();
                    
                    // Save vào database
                    productOrderRepository.save(order);
                }
            }
            
            // Transaction sẽ được commit tự động khi method kết thúc thành công
            return "SUCCESS";
            
        } catch (Exception e) {
            // Transaction sẽ được rollback tự động khi có exception
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }
}
