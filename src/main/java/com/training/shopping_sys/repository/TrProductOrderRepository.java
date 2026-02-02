package com.training.shopping_sys.repository;

import com.training.shopping_sys.entity.TrProductOrder;
import com.training.shopping_sys.entity.TrProductOrderKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Product Order Repository.
 * 
 * <p>Data access interface for {@link TrProductOrder} entity.
 * Handles order transactions with composite primary key.</p>
 * 
 * <p>Provides aggregation queries for calculating total ordered amounts
 * per product and finding maximum order ID for new order generation.</p>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface TrProductOrderRepository extends JpaRepository<TrProductOrder, TrProductOrderKey> {
    
    /**
     * Calculate total ordered amount for a product.
     * 
     * <p>Sums up all order quantities for the specified product across
     * all orders. Returns 0 if no orders found.</p>
     * 
     * @param productId ID of the product
     * @return Total quantity ordered (0 if no orders)
     */
    @Query("SELECT COALESCE(SUM(t.orderProductAmount), 0) FROM TrProductOrder t WHERE t.id.productId = :productId")
    Integer getTotalOrderedAmount(@Param("productId") Long productId);
    
    /**
     * Find maximum order ID in the database.
     * 
     * <p>Returns the highest order_id value currently in the system.
     * Used to generate new sequential order IDs. Returns null if no orders exist.</p>
     * 
     * @return Maximum order ID, or null if no orders
     */
    @Query("SELECT MAX(t.id.orderId) FROM TrProductOrder t")
    Long findMaxOrderId();
}
