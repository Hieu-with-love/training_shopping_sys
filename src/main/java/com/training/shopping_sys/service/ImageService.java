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
import java.util.UUID;

/**
 * Image Service.
 * 
 * <p>Handles image upload, storage, and management for product images.
 * Supports multiple storage strategies:</p>
 * <ul>
 *   <li>Database storage as byte arrays</li>
 *   <li>File system storage in configurable directory</li>
 * </ul>
 * 
 * <p>Provides functionality for:
 * - Uploading images from multipart files
 * - Bulk loading images from file system
 * - Image validation and format checking
 * </p>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {
    
    private final MstProductRepository productRepository;
    private final ImageUtil imageUtil;
    
    @Value("${image.storage.path:uploads/products}")
    private String imageStoragePath;
    
    /**
     * Update product image from multipart file upload.
     * 
     * <p>Validates file type, saves to file system with UUID-based filename,
     * and stores byte array in database. Creates storage directory if not exists.</p>
     * 
     * @param productId ID of the product to update
     * @param file Uploaded image file (must be valid image format)
     * @return true if update successful, false otherwise
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
            String fileName = "product_" + UUID.randomUUID() + extension;
            
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
     * Update product image from file system path.
     * 
     * <p>Reads image from specified file path and stores in database.
     * Useful for batch loading images from existing files.</p>
     * 
     * @param productId ID of the product to update
     * @param imagePath File system path to the image file
     * @return true if update successful, false otherwise
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
     * Bulk load images from img folder to database.
     * 
     * <p>Scans img/ folder for product images following naming convention:
     * product_{productId}.jpg or product_{productId}.png. Loads images
     * for products 1-20 and stores in database.</p>
     * 
     * <p>Tries JPG format first, falls back to PNG if JPG not found.</p>
     * 
     * @return Number of images successfully loaded
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
