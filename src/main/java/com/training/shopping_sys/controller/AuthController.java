package com.training.shopping_sys.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Authentication Controller.
 * 
 * <p>Handles authentication-related HTTP requests including login
 * page display and logout confirmation. Works with Spring Security
 * for actual authentication processing.</p>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
@Controller
public class AuthController {
    
    /**
     * Display login page.
     * 
     * <p>Shows login form and handles error/logout messages from
     * Spring Security. Error parameter indicates failed login attempt,
     * logout parameter indicates successful logout.</p>
     * 
     * @param error Error flag from Spring Security (optional)
     * @param logout Logout flag from Spring Security (optional)
     * @param model Spring MVC Model for passing messages to view
     * @return Template name "login"
     */
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng!");
        }
        if (logout != null) {
            model.addAttribute("message", "Bạn đã đăng xuất thành công!");
        }
        return "login";
    }
    
    /**
     * Display products page (legacy endpoint).
     * 
     * <p>Returns the products template. This appears to be a legacy
     * endpoint; the main product listing is at /products/list.</p>
     * 
     * @return Template name "products"
     */
    @GetMapping("/products")
    public String products() {
        return "products";
    }
}
