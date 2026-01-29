package com.training.shopping_sys.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mstproduct")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MstProduct {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;
    
    @Column(name = "product_name", length = 400)
    private String productName;
    
    @Column(name = "product_description", length = 400)
    private String productDescription;
    
    @Column(name = "product_img")
    private String productImg;
    
    @Column(name = "producttype_id")
    private Long producttypeId;
    
    @ManyToOne
    @JoinColumn(name = "producttype_id", insertable = false, updatable = false)
    private MstProductType productType;
    
    @Column(name = "status", length = 1)
    private String status; // '0' = active, '1' = deleted
}
