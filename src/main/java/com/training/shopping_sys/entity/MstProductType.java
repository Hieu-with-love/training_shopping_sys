package com.training.shopping_sys.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mstproducttype")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MstProductType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "producttype_id")
    private Long producttypeId;
    
    @Column(name = "producttype_name", length = 400)
    private String producttypeName;
    
    @Column(name = "status", length = 1)
    private String status; // '0' = active, '1' = deleted
}
