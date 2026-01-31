package com.training.shopping_sys.repository;

import com.training.shopping_sys.entity.TrProductOrder;
import com.training.shopping_sys.entity.TrProductOrderKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TrProductOrderRepository extends JpaRepository<TrProductOrder, TrProductOrderKey> {
    
    @Query("SELECT COALESCE(SUM(t.orderProductAmount), 0) FROM TrProductOrder t WHERE t.id.productId = :productId")
    Integer getTotalOrderedAmount(@Param("productId") Long productId);
}
