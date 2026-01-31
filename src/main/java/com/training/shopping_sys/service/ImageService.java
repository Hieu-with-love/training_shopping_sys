package com.training.shopping_sys.service;

import com.training.shopping_sys.entity.MstProduct;
import com.training.shopping_sys.repository.MstProductRepository;
import com.training.shopping_sys.util.ImageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {
    
    private final MstProductRepository productRepository;
    private final ImageUtil imageUtil;
    
    @Value("${image.storage.path:img/}")
    private String imageStoragePath;
    
    /**
     * Cập nhật ảnh cho sản phẩm từ MultipartFile
     * Lưu file vào thư mục img/ và lưu byte[] vào DB
     * @param productId ID của sản phẩm
     * @param file File ảnh upload
     * @return true nếu cập nhật thành công
     */
    @Transactional
    public boolean updateProductImage(Long productId, MultipartFile file) {
        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                log.warn("File is empty");
                return false;
            }
            
            // Validate file extension
            String originalFilename = file.getOriginalFilename();
            if (!imageUtil.isValidImageFile(originalFilename)) {
                log.warn("Invalid image file type: {}", originalFilename);
                return false;
            }
            
            // Get product
            Optional<MstProduct> productOpt = productRepository.findById(productId);
            if (productOpt.isEmpty()) {
                log.warn("Product not found: {}", productId);
                return false;
            }
            
            // Get file extension
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = "product_" + productId + extension;
            
            // Ensure directory exists
            Path directory = Paths.get(imageStoragePath);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }
            
            // Save file to img/ folder
            Path filePath = directory.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Also save to database for backup/compatibility
            MstProduct product = productOpt.get();
            product.setProductImg(file.getBytes());
            productRepository.save(product);
            
            log.info("Updated image for product {}: {} (saved to {})", productId, originalFilename, filePath);
            return true;
            
        } catch (IOException e) {
            log.error("Error updating product image for product {}", productId, e);
            return false;
        }
    }
    
    /**
     * Cập nhật ảnh cho sản phẩm từ file path
     * @param productId ID của sản phẩm
     * @param imagePath Đường dẫn tới file ảnh
     * @return true nếu cập nhật thành công
     */
    @Transactional
    public boolean updateProductImageFromPath(Long productId, String imagePath) {
        try {
            // Read image
            byte[] imageBytes = imageUtil.readImageFromFile(imagePath);
            if (imageBytes == null) {
                log.warn("Cannot read image from path: {}", imagePath);
                return false;
            }
            
            // Get product
            Optional<MstProduct> productOpt = productRepository.findById(productId);
            if (productOpt.isEmpty()) {
                log.warn("Product not found: {}", productId);
                return false;
            }
            
            // Update image
            MstProduct product = productOpt.get();
            product.setProductImg(imageBytes);
            productRepository.save(product);
            
            log.info("Updated image for product {} from path: {}", productId, imagePath);
            return true;
            
        } catch (Exception e) {
            log.error("Error updating product image from path for product {}", productId, e);
            return false;
        }
    }
    
    /**
     * Đọc và cập nhật ảnh cho nhiều sản phẩm từ thư mục img
     * Quy ước tên file: product_{productId}.jpg hoặc product_{productId}.png
     * Ví dụ: product_1.jpg, product_2.png
     */
    @Transactional
    public int loadImagesFromImgFolder() {
        int count = 0;
        
        // Load images for products 1-20
        for (long i = 1; i <= 20; i++) {
            // Try jpg first
            byte[] imageBytes = imageUtil.readImageFromImgFolder("product_" + i + ".jpg");
            
            // If not found, try png
            if (imageBytes == null) {
                imageBytes = imageUtil.readImageFromImgFolder("product_" + i + ".png");
            }
            
            // If found, update product
            if (imageBytes != null) {
                Optional<MstProduct> productOpt = productRepository.findById(i);
                if (productOpt.isPresent()) {
                    MstProduct product = productOpt.get();
                    product.setProductImg(imageBytes);
                    productRepository.save(product);
                    count++;
                    log.info("Loaded image for product {}", i);
                }
            }
        }
        
        log.info("Loaded {} product images from img folder", count);
        return count;
    }
}
