package com.training.shopping_sys.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Product Order Transaction Entity.
 * 
 * <p>Represents a product order transaction in the shopping system.
 * This entity tracks which products were ordered, by whom, in what quantity,
 * and when the order was placed.</p>
 * 
 * <p>The composite primary key is defined by {@link TrProductOrderKey},
 * which includes order ID, customer name, and product ID.</p>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "trproductorder")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrProductOrder {
    
    /** Composite primary key containing order ID, customer name, and product ID. */
    @EmbeddedId
    private TrProductOrderKey id;
    
    /** Associated product entity. Many-to-one relationship. */
    @ManyToOne
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private MstProduct product;
    
    /** Quantity of the product ordered. */
    @Column(name = "order_product_amount")
    private Integer orderProductAmount;
    
    /** Date and time when the order was placed. */
    @Column(name = "order_date")
    private LocalDateTime orderDate;
}
