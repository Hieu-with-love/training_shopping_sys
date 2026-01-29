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
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;
    
    @Column(name = "product_id")
    private Long productId;
    
    @ManyToOne
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private MstProduct product;
    
    @Column(name = "quantity")
    private Integer quantity;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "order_date")
    private LocalDateTime orderDate;
}
