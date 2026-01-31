package com.training.shopping_sys.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
public class ImageUtil {
    
    /**
     * Đọc ảnh từ file path
     * @param imagePath Đường dẫn tới file ảnh
     * @return byte array của ảnh hoặc null nếu lỗi
     */
    public byte[] readImageFromFile(String imagePath) {
        try {
            Path path = Paths.get(imagePath);
            if (Files.exists(path)) {
                return Files.readAllBytes(path);
            } else {
                log.warn("Image file not found: {}", imagePath);
                return null;
            }
        } catch (IOException e) {
            log.error("Error reading image file: {}", imagePath, e);
            return null;
        }
    }
    
    /**
     * Đọc ảnh từ thư mục img với tên file
     * @param fileName Tên file (ví dụ: product1.jpg)
     * @return byte array của ảnh hoặc null nếu lỗi
     */
    public byte[] readImageFromImgFolder(String fileName) {
        String imgFolderPath = "img/" + fileName;
        return readImageFromFile(imgFolderPath);
    }
    
    /**
     * Validate file extension
     * @param fileName Tên file
     * @return true nếu là png/jpg/jpeg
     */
    public boolean isValidImageFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        String lowerCaseFileName = fileName.toLowerCase();
        return lowerCaseFileName.endsWith(".png") || 
               lowerCaseFileName.endsWith(".jpg") || 
               lowerCaseFileName.endsWith(".jpeg");
    }
    
    /**
     * Get content type từ file name
     * @param fileName Tên file
     * @return Content type string
     */
    public String getContentType(String fileName) {
        if (fileName == null) {
            return "application/octet-stream";
        }
        String lowerCaseFileName = fileName.toLowerCase();
        if (lowerCaseFileName.endsWith(".png")) {
            return "image/png";
        } else if (lowerCaseFileName.endsWith(".jpg") || lowerCaseFileName.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        return "application/octet-stream";
    }
    
    /**
     * Detect content type từ byte array dựa vào magic bytes
     * @param imageBytes Byte array của ảnh
     * @return Content type string
     */
    public String detectContentTypeFromBytes(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length < 8) {
            return "application/octet-stream";
        }
        
        // Check PNG signature: 89 50 4E 47 0D 0A 1A 0A
        if (imageBytes.length >= 8 &&
            imageBytes[0] == (byte) 0x89 &&
            imageBytes[1] == 0x50 &&
            imageBytes[2] == 0x4E &&
            imageBytes[3] == 0x47) {
            return "image/png";
        }
        
        // Check JPEG signature: FF D8 FF
        if (imageBytes.length >= 3 &&
            imageBytes[0] == (byte) 0xFF &&
            imageBytes[1] == (byte) 0xD8 &&
            imageBytes[2] == (byte) 0xFF) {
            return "image/jpeg";
        }
        
        // Check GIF signature: GIF87a or GIF89a
        if (imageBytes.length >= 6 &&
            imageBytes[0] == 0x47 && // 'G'
            imageBytes[1] == 0x49 && // 'I'
            imageBytes[2] == 0x46) { // 'F'
            return "image/gif";
        }
        
        // Check WebP signature: RIFF....WEBP
        if (imageBytes.length >= 12 &&
            imageBytes[0] == 0x52 && // 'R'
            imageBytes[1] == 0x49 && // 'I'
            imageBytes[2] == 0x46 && // 'F'
            imageBytes[3] == 0x46 && // 'F'
            imageBytes[8] == 0x57 && // 'W'
            imageBytes[9] == 0x45 && // 'E'
            imageBytes[10] == 0x42 && // 'B'
            imageBytes[11] == 0x50) { // 'P'
            return "image/webp";
        }
        
        // Default to JPEG if cannot detect
        return "image/jpeg";
    }
    
    /**
     * Get MediaType từ byte array
     * @param imageBytes Byte array của ảnh
     * @return MediaType object
     */
    public org.springframework.http.MediaType getMediaTypeFromBytes(byte[] imageBytes) {
        String contentType = detectContentTypeFromBytes(imageBytes);
        return org.springframework.http.MediaType.parseMediaType(contentType);
    }
}
