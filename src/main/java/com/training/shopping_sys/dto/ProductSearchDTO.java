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
    private String productImg;
    private Long producttypeId;
    private String producttypeName;
    private Long totalOrdered;
}
