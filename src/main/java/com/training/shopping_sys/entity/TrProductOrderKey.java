package com.training.shopping_sys.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Composite Primary Key for Product Order Transaction.
 * 
 * <p>This embeddable class represents the composite primary key for the
 * {@link TrProductOrder} entity. It uniquely identifies an order line item
 * using a combination of order ID, customer name, and product ID.</p>
 * 
 * <p>Implements {@link Serializable} as required for composite keys in JPA.
 * Includes proper equals() and hashCode() implementations for key comparison.</p>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrProductOrderKey implements Serializable {
    
    /** Order identifier. Part of the composite key. */
    @Column(name = "order_id")
    private Long orderId;
    
    /** Customer name who placed the order. Part of the composite key. Maximum length: 400 characters. */
    @Column(name = "customer_name", length = 400)
    private String customerName;
    
    /** Product identifier. Part of the composite key. */
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
