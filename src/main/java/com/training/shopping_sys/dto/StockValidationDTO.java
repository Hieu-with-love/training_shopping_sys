package com.training.shopping_sys.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockValidationDTO {
    private boolean valid;
    private String message;
    private Integer availableStock;
    private String productName;
}
