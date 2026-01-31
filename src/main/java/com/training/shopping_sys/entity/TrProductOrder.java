package com.training.shopping_sys.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "trproductorder")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrProductOrder {
    
    @EmbeddedId
    private TrProductOrderKey id;
    
    @ManyToOne
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private MstProduct product;
    
    @Column(name = "order_product_amount")
    private Integer orderProductAmount;
    
    @Column(name = "order_date")
    private LocalDateTime orderDate;
}
