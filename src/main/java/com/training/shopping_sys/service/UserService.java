package com.training.shopping_sys.service;

import com.training.shopping_sys.entity.User;
import com.training.shopping_sys.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * User Service.
 * 
 * <p>Service xử lý logic nghiệp vụ liên quan đến user, bao gồm:
 * - Lấy thông tin user để hiển thị
 * - Lấy tên user</p>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    /**
     * ⑧ Lấy thông tin user để hiển thị.
     * 
     * <p>Trả về chuỗi thông tin user theo format: "Xin chào [Username]"</p>
     * 
     * @param username Username của user
     * @return Chuỗi thông tin user
     */
    public String getUserInfo(String username) {
        return userRepository.findByUsername(username)
                .map(user -> "Xin chào " + user.getUsername())
                .orElse("Xin chào");
    }
    
    /**
     * ① Lấy tên user từ database.
     * 
     * <p>Trả về username của user, dùng để điền mặc định vào field "Người đặt hàng"</p>
     * 
     * @param username Username của user
     * @return Username, hoặc empty string nếu không tìm thấy
     */
    public String getUserName(String username) {
        return userRepository.findByUsername(username)
                .map(User::getUsername)
                .orElse("");
    }
}
