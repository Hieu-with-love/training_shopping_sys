package com.training.shopping_sys.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Image Utility Class.
 * 
 * <p>Provides utility methods for image handling including:
 * - Reading images from file system
 * - Validating image file formats
 * - Detecting content types from file extensions and magic bytes
 * </p>
 * 
 * <p>Supports common image formats: PNG, JPEG, GIF, WebP.</p>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Component
public class ImageUtil {
    
    /**
     * Read image from file path.
     * 
     * @param imagePath Absolute or relative path to image file
     * @return Byte array of image data, or null if file not found or error occurs
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
     * Read image from img folder.
     * 
     * <p>Convenience method to read images from the img/ directory.
     * Prepends "img/" to the filename.</p>
     * 
     * @param fileName File name (e.g., "product_1.jpg")
     * @return Byte array of image data, or null if not found
     */
    public byte[] readImageFromImgFolder(String fileName) {
        String imgFolderPath = "img/" + fileName;
        return readImageFromFile(imgFolderPath);
    }
    
    /**
     * Validate if file has valid image extension.
     * 
     * @param fileName File name to validate
     * @return true if extension is .png, .jpg, or .jpeg (case-insensitive)
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
     * Get MIME content type from file extension.
     * 
     * @param fileName File name with extension
     * @return MIME type string ("image/png", "image/jpeg", or "application/octet-stream")
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
     * Detect content type from byte array using magic bytes.
     * 
     * <p>Examines the first few bytes of the image data to determine
     * the file format. Supports PNG, JPEG, GIF, and WebP detection.</p>
     * 
     * @param imageBytes Byte array of image data
     * @return MIME type string based on detected format, defaults to "image/jpeg"
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
     * Get Spring MediaType from byte array.
     * 
     * <p>Converts detected content type string to Spring's MediaType object.
     * Useful for HTTP responses.</p>
     * 
     * @param imageBytes Byte array of image data
     * @return Spring MediaType object
     */
    public org.springframework.http.MediaType getMediaTypeFromBytes(byte[] imageBytes) {
        String contentType = detectContentTypeFromBytes(imageBytes);
        return org.springframework.http.MediaType.parseMediaType(contentType);
    }
}
