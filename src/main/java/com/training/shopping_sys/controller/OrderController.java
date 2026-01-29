package com.training.shopping_sys.controller;

import com.training.shopping_sys.entity.MstProduct;
import com.training.shopping_sys.entity.TrProductOrder;
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

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final MstProductRepository productRepository;
    private final TrProductOrderRepository orderRepository;
    
    @GetMapping("/place")
    public String showOrderPage(
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            Model model) {
        
        Optional<MstProduct> productOpt = productRepository.findById(productId);
        
        if (productOpt.isEmpty()) {
            model.addAttribute("error", "Sản phẩm không tồn tại!");
            return "redirect:/products/list";
        }
        
        MstProduct product = productOpt.get();
        
        // Get current user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        model.addAttribute("product", product);
        model.addAttribute("quantity", quantity);
        model.addAttribute("username", username);
        
        return "order";
    }
    
    @PostMapping("/confirm")
    public String confirmOrder(
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Get current user info
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            // Create order
            TrProductOrder order = new TrProductOrder();
            order.setProductId(productId);
            order.setQuantity(quantity);
            order.setUserId(1L); // TODO: Get real user ID from authentication
            order.setOrderDate(LocalDateTime.now());
            
            orderRepository.save(order);
            
            redirectAttributes.addFlashAttribute("message", 
                "Đặt hàng thành công! Đơn hàng của bạn đã được ghi nhận.");
            
            return "redirect:/orders/success";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Có lỗi xảy ra khi đặt hàng: " + e.getMessage());
            return "redirect:/products/list";
        }
    }
    
    @GetMapping("/success")
    public String orderSuccess() {
        return "order-success";
    }
}
