package com.training.shopping_sys.controller;

import com.training.shopping_sys.dto.OrderItemDTO;
import com.training.shopping_sys.entity.MstProduct;
import com.training.shopping_sys.entity.TrProductOrder;
import com.training.shopping_sys.entity.TrProductOrderKey;
import com.training.shopping_sys.repository.MstProductRepository;
import com.training.shopping_sys.repository.TrProductOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Map;

/**
 * Order Controller.
 * 
 * <p>Handles HTTP requests related to order processing including:
 * order placement, confirmation, and success pages. Validates stock
 * availability before creating orders.</p>
 * 
 * <p>Orders are associated with authenticated users and include
 * timestamp tracking. Supports both single and multiple product orders.</p>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final MstProductRepository productRepository;
    private final TrProductOrderRepository orderRepository;
    
    /**
     * Display order confirmation page for multiple products.
     * 
     * <p>Processes order request from product list page, validates stock
     * availability for each product, and displays order confirmation form.
     * Preserves search parameters for cancel navigation.</p>
     * 
     * @param request HTTP request containing product IDs and quantities
     * @param keyword Search keyword to preserve (optional)
     * @param producttypeId Product type filter to preserve (optional)
     * @param page Page number to preserve
     * @param model Spring MVC Model to pass data to view
     * @param redirectAttributes Attributes for redirect scenarios
     * @return Template name "order" or redirect to product list on error
     */
    @PostMapping("/place")
    public String showOrderPage(
            HttpServletRequest request,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "producttypeId", required = false) Long producttypeId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        // Parse products từ request parameters
        Map<String, String[]> params = request.getParameterMap();
        List<Long> productIds = new ArrayList<>();
        List<Integer> quantities = new ArrayList<>();
        
        // Tìm tất cả products[i].productId và products[i].quantity
        int index = 0;
        while (true) {
            String productIdKey = "products[" + index + "].productId";
            String quantityKey = "products[" + index + "].quantity";
            
            if (params.containsKey(productIdKey) && params.containsKey(quantityKey)) {
                productIds.add(Long.parseLong(params.get(productIdKey)[0]));
                quantities.add(Integer.parseInt(params.get(quantityKey)[0]));
                index++;
            } else {
                break;
            }
        }
        
        // Kiểm tra có sản phẩm nào không
        if (productIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không có sản phẩm nào được chọn!");
            return "redirect:/products/list";
        }
        
        List<OrderItemDTO> orderItems = new ArrayList<>();
        
        // Xử lý từng sản phẩm
        for (int i = 0; i < productIds.size(); i++) {
            Long productId = productIds.get(i);
            Integer quantity = quantities.get(i);
            
            Optional<MstProduct> productOpt = productRepository.findById(productId);
            
            if (productOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại!");
                return "redirect:/products/list";
            }
            
            MstProduct mstProduct = productOpt.get();
            
            // Validate stock availability
            Integer totalOrdered = orderRepository.getTotalOrderedAmount(productId);
            Integer availableStock = (mstProduct.getProductAmount() != null ? mstProduct.getProductAmount() : 0) - totalOrdered;
            
            if (quantity > availableStock) {
                redirectAttributes.addFlashAttribute("error", 
                    String.format("Số lượng đặt hàng của sản phẩm %s không đủ trong kho. Xin hãy nhập số lượng <= %d", 
                        mstProduct.getProductName(), availableStock));
                return "redirect:/products/list";
            }
            
            // Tạo OrderItemDTO
            OrderItemDTO item = new OrderItemDTO();
            item.setProductId(productId);
            item.setProductName(mstProduct.getProductName());
            item.setQuantity(quantity);
            item.setAvailableStock(availableStock);
            item.setProducttypeName(mstProduct.getProductType() != null ? mstProduct.getProductType().getProducttypeName() : "");
            
            orderItems.add(item);
        }
        
        // Get current user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        model.addAttribute("orderItems", orderItems);
        model.addAttribute("username", username);
        // Lưu search params để truyền lại khi Cancel
        model.addAttribute("keyword", keyword);
        model.addAttribute("producttypeId", producttypeId);
        model.addAttribute("page", page);
        model.addAttribute("productIds", productIds);
        model.addAttribute("quantities", quantities);
        
        return "order";
    }
    
    /**
     * Display order confirmation page for a single product.
     * 
     * <p>Simpler version of order placement for single product orders.
     * Validates stock availability and displays confirmation form.</p>
     * 
     * @param productId ID of the product to order
     * @param quantity Quantity to order
     * @param model Spring MVC Model to pass data to view
     * @param redirectAttributes Attributes for redirect scenarios
     * @return Template name "order" or redirect to product list on error
     */
    @GetMapping("/place")
    public String showOrderPageSingle(
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        Optional<MstProduct> productOpt = productRepository.findById(productId);
        
        if (productOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại!");
            return "redirect:/products/list";
        }
        
        MstProduct product = productOpt.get();
        
        // Validate stock availability
        Integer totalOrdered = orderRepository.getTotalOrderedAmount(productId);
        Integer availableStock = (product.getProductAmount() != null ? product.getProductAmount() : 0) - totalOrdered;
        
        if (quantity > availableStock) {
            redirectAttributes.addFlashAttribute("error", 
                String.format("Số lượng đặt hàng của sản phẩm %s không đủ trong kho. Xin hãy nhập số lượng <= %d", 
                    product.getProductName(), availableStock));
            return "redirect:/products/list";
        }
        
        List<OrderItemDTO> orderItems = new ArrayList<>();
        OrderItemDTO item = new OrderItemDTO();
        item.setProductId(productId);
        item.setProductName(product.getProductName());
        item.setQuantity(quantity);
        item.setAvailableStock(availableStock);
        item.setProducttypeName(product.getProductType() != null ? product.getProductType().getProducttypeName() : "");
        orderItems.add(item);
        
        // Get current user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        model.addAttribute("orderItems", orderItems);
        model.addAttribute("username", username);
        
        return "order";
    }
    
    /**
     * Confirm and process the order.
     * 
     * <p>Creates order records in the database for all selected products.
     * Performs final stock validation before saving. All products in a
     * single order share the same order ID (timestamp-based).</p>
     * 
     * @param productIds List of product IDs to order
     * @param quantities List of quantities corresponding to each product
     * @param redirectAttributes Attributes for success/error messages
     * @return Redirect to success page or product list on error
     */
    @PostMapping("/confirm")
    public String confirmOrder(
            @RequestParam("productIds") List<Long> productIds,
            @RequestParam("quantities") List<Integer> quantities,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Get current user info
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            // Generate order ID một lần cho tất cả sản phẩm
            Long orderId = System.currentTimeMillis();
            
            // Xử lý từng sản phẩm
            for (int i = 0; i < productIds.size(); i++) {
                Long productId = productIds.get(i);
                Integer quantity = quantities.get(i);
                
                // Validate product exists
                Optional<MstProduct> productOpt = productRepository.findById(productId);
                if (productOpt.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại!");
                    return "redirect:/products/list";
                }
                
                MstProduct product = productOpt.get();
                
                // Validate stock availability again before confirming
                Integer totalOrdered = orderRepository.getTotalOrderedAmount(productId);
                Integer availableStock = (product.getProductAmount() != null ? product.getProductAmount() : 0) - totalOrdered;
                
                if (quantity > availableStock) {
                    redirectAttributes.addFlashAttribute("error", 
                        String.format("Số lượng đặt hàng của sản phẩm %s không đủ trong kho. Xin hãy nhập số lượng <= %d", 
                            product.getProductName(), availableStock));
                    return "redirect:/products/list";
                }
                
                // Create composite key
                TrProductOrderKey orderKey = new TrProductOrderKey();
                orderKey.setOrderId(orderId);
                orderKey.setCustomerName(username);
                orderKey.setProductId(productId);
                
                // Create order
                TrProductOrder order = new TrProductOrder();
                order.setId(orderKey);
                order.setOrderProductAmount(quantity);
                order.setOrderDate(LocalDateTime.now());
                
                orderRepository.save(order);
            }
            
            redirectAttributes.addFlashAttribute("message", 
                "Đặt hàng thành công! Đơn hàng của bạn đã được ghi nhận.");
            
            // Redirect về init state (không có search params)
            return "redirect:/orders/success";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Có lỗi xảy ra khi đặt hàng: " + e.getMessage());
            // Redirect về init state khi có lỗi
            return "redirect:/products/list";
        }
    }
    
    /**
     * Display order success page.
     * 
     * <p>Shows confirmation message after successful order placement.
     * Redirects user to a clean state without search parameters.</p>
     * 
     * @param redirectAttributes Attributes for success message
     * @return Template name "order-success"
     */
    @GetMapping("/success")
    public String orderSuccess(RedirectAttributes redirectAttributes) {
        // Sau khi success, về trang init (không params)
        redirectAttributes.addFlashAttribute("successMessage", 
            "Đặt hàng thành công! Đơn hàng của bạn đã được ghi nhận.");
        return "order-success";
    }
}
