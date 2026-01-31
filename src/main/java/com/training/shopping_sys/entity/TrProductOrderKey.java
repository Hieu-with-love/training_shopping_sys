package com.training.shopping_sys.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrProductOrderKey implements Serializable {
    
    @Column(name = "order_id")
    private Long orderId;
    
    @Column(name = "customer_name", length = 400)
    private String customerName;
    
    @Column(name = "product_id")
    private Long productId;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        TrProductOrderKey that = (TrProductOrderKey) o;
        
        if (orderId != null ? !orderId.equals(that.orderId) : that.orderId != null) return false;
        if (customerName != null ? !customerName.equals(that.customerName) : that.customerName != null) return false;
        return productId != null ? productId.equals(that.productId) : that.productId == null;
    }
    
    @Override
    public int hashCode() {
        int result = orderId != null ? orderId.hashCode() : 0;
        result = 31 * result + (customerName != null ? customerName.hashCode() : 0);
        result = 31 * result + (productId != null ? productId.hashCode() : 0);
        return result;
    }
}
