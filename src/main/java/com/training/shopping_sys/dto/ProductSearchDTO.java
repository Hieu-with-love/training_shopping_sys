package com.training.shopping_sys.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchDTO {
    private Long productId;
    private String productName;
    private String productDescription;
    private boolean hasImage; // Indicates if product has an image
    private String imageUrl; // URL to image file (e.g., /img/product_1.jpg)
    private Long producttypeId;
    private String producttypeName;
    private Long totalOrdered;
}
