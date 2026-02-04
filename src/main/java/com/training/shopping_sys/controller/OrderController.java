package com.training.shopping_sys.controller;

import com.training.shopping_sys.dto.OrderProductDTO;
import com.training.shopping_sys.dto.OrderRequestDTO;
import com.training.shopping_sys.service.OrderService;
import com.training.shopping_sys.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * Order Controller.
 * 
 * <p>Xử lý các HTTP request liên quan đến đặt hàng, bao gồm:
 * - Hiển thị màn hình đặt hàng
 * - Submit đơn đặt hàng
 * - Validation thông tin đặt hàng</p>
 * 
 * <p>Controller làm việc với session để lưu trữ thông tin sản phẩm đã chọn
 * và với OrderService để xử lý logic nghiệp vụ.</p>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    private final UserService userService;
    
    /**
     * Hiển thị màn hình đặt hàng.
     * 
     * <p>Lấy thông tin user đăng nhập, tên user từ database, và danh sách
     * sản phẩm đã chọn từ session. Tính toán số lượng khả dụng cho mỗi sản phẩm.</p>
     * 
     * @param session HTTP Session chứa user ID và danh sách sản phẩm
     * @param model Spring MVC Model để truyền dữ liệu tới view
     * @return Template name "order" hoặc redirect tới login nếu chưa đăng nhập
     */
    @GetMapping
    public String showOrderPage(HttpSession session, Model model) {
        // Lấy thông tin user đăng nhập
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth != null ? auth.getName() : null;
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        // ⑧ Hiển thị thông tin user
        String userInfo = userService.getUserInfo(userId);
        model.addAttribute("userInfo", userInfo);
        
        // ① Lấy tên user từ DB
        String userName = userService.getUserName(userId);
        model.addAttribute("customerName", userName);
        
        // ② ③ Khởi tạo rỗng
        model.addAttribute("deliveryAddress", "");
        model.addAttribute("deliveryDate", "");
        
        // ④ Lấy danh sách sản phẩm đã chọn từ session
        @SuppressWarnings("unchecked")
        List<OrderProductDTO> sessionProducts = (List<OrderProductDTO>) session.getAttribute("orderProducts");
        
        List<OrderProductDTO> orderProducts = null;
        if (sessionProducts != null && !sessionProducts.isEmpty()) {
            // Tạo ArrayList mới để tránh UnmodifiableList issue
            orderProducts = new ArrayList<>(sessionProducts);
            // Tính số lượng available cho mỗi sản phẩm
            orderProducts = orderService.calculateAvailableQuantity(orderProducts);
        }
        
        model.addAttribute("orderProducts", orderProducts);
        
        return "order";
    }
    
    /**
     * ⑤ Xử lý submit đặt hàng.
     * 
     * <p>Controller Layer: Điều hướng + Transaction Management
     * - Xác thực user đăng nhập
     * - Gọi Service để validate nghiệp vụ và xử lý insert
     * - Xử lý transaction commit/rollback (đã có @Transactional ở Service)
     * - Điều hướng về màn hình phù hợp với kết quả
     * - Hiển thị message thông qua alert và focus vào field lỗi</p>
     * 
     * @param orderRequest DTO chứa thông tin đặt hàng từ form
     * @param session HTTP Session để xóa orderProducts sau khi đặt hàng thành công
     * @param model Spring MVC Model để truyền dữ liệu khi có lỗi
     * @param redirectAttributes Attributes để truyền success message
     * @return Template name "order" nếu có lỗi, redirect tới products nếu thành công
     */
    @PostMapping("/submit")
    public String submitOrder(
            @ModelAttribute OrderRequestDTO orderRequest,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth != null ? auth.getName() : null;
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        try {
            // Gọi Service layer để validate nghiệp vụ và xử lý insert
            // Service sẽ tự động commit/rollback thông qua @Transactional
            String result = orderService.processOrder(orderRequest, userId);
            
            if (result.startsWith("ERROR:")) {
                // Có lỗi validation - trả về từ Service layer
                String[] parts = result.split(":", 3);
                String errorMessage = parts[1];
                String focusField = parts.length > 2 ? parts[2] : "";
                
                // Giữ lại dữ liệu đã nhập để hiển thị lại form
                model.addAttribute("customerName", orderRequest.getCustomerName());
                model.addAttribute("deliveryAddress", orderRequest.getDeliveryAddress());
                model.addAttribute("deliveryDate", orderRequest.getDeliveryDate());
                
                // Lấy lại danh sách sản phẩm từ session và tính toán available quantity
                @SuppressWarnings("unchecked")
                List<OrderProductDTO> sessionProducts = (List<OrderProductDTO>) session.getAttribute("orderProducts");
                if (sessionProducts != null && !sessionProducts.isEmpty()) {
                    List<OrderProductDTO> orderProducts = new ArrayList<>(sessionProducts);
                    // Cập nhật số lượng đặt hàng từ request
                    if (orderRequest.getProducts() != null) {
                        for (int i = 0; i < orderProducts.size() && i < orderRequest.getProducts().size(); i++) {
                            orderProducts.get(i).setOrderQuantity(orderRequest.getProducts().get(i).getOrderQuantity());
                        }
                    }
                    orderProducts = orderService.calculateAvailableQuantity(orderProducts);
                    model.addAttribute("orderProducts", orderProducts);
                }
                
                model.addAttribute("errorMessage", errorMessage);
                model.addAttribute("focusField", focusField);
                
                // Hiển thị lại thông tin user
                String userInfo = userService.getUserInfo(userId);
                model.addAttribute("userInfo", userInfo);
                
                return "order";
            }
            
            // Thành công - Transaction đã được commit tự động bởi @Transactional
            // Clear all session data liên quan đến order và search
            session.removeAttribute("orderProducts");
            session.removeAttribute("searchKeyword");
            session.removeAttribute("searchProductTypeId");
            session.removeAttribute("searchPage");
            session.removeAttribute("productQuantities");
            redirectAttributes.addFlashAttribute("successMessage", "Xử lý đặt hàng đã thành công");
            return "redirect:/products/list";
            
        } catch (Exception e) {
            // Exception - Transaction đã được rollback tự động bởi @Transactional
            e.printStackTrace();
            
            // Giữ lại dữ liệu
            model.addAttribute("customerName", orderRequest.getCustomerName());
            model.addAttribute("deliveryAddress", orderRequest.getDeliveryAddress());
            model.addAttribute("deliveryDate", orderRequest.getDeliveryDate());
            
            // Lấy lại danh sách sản phẩm từ session
            @SuppressWarnings("unchecked")
            List<OrderProductDTO> sessionProducts = (List<OrderProductDTO>) session.getAttribute("orderProducts");
            if (sessionProducts != null && !sessionProducts.isEmpty()) {
                List<OrderProductDTO> orderProducts = new ArrayList<>(sessionProducts);
                if (orderRequest.getProducts() != null) {
                    for (int i = 0; i < orderProducts.size() && i < orderRequest.getProducts().size(); i++) {
                        orderProducts.get(i).setOrderQuantity(orderRequest.getProducts().get(i).getOrderQuantity());
                    }
                }
                orderProducts = orderService.calculateAvailableQuantity(orderProducts);
                model.addAttribute("orderProducts", orderProducts);
            }
            
            model.addAttribute("errorMessage", "Xử lý đặt hàng thất bại");
            
            String userInfo = userService.getUserInfo(userId);
            model.addAttribute("userInfo", userInfo);
            
            return "order";
        }
    }
    
    /**
     * Xử lý cancel order - quay về product list với search state được khôi phục.
     * 
     * <p>Lấy search state từ session (keyword, producttypeId, page) và
     * redirect về product list page với các tham số này.</p>
     * 
     * @param session HTTP Session chứa search state
     * @return Redirect tới product list với search parameters
     */
    @GetMapping("/cancel")
    public String cancelOrder(HttpSession session) {
        // Không xóa session data để có thể restore
        // Chỉ redirect về product list, ProductController sẽ tự restore từ session
        return "redirect:/products/list";
    }
}

