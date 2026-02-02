package com.training.shopping_sys.repository;

import com.training.shopping_sys.entity.TrProductOrder;
import com.training.shopping_sys.entity.TrProductOrderKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Product Order Repository.
 * 
 * <p>Data access interface for {@link TrProductOrder} entity.
 * Handles order transactions with composite primary key.</p>
 * 
 * <p>Optimized using JPA derived query methods and JPQL for efficient
 * aggregation and maximum value retrieval.</p>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface TrProductOrderRepository extends JpaRepository<TrProductOrder, TrProductOrderKey> {
    
    /**
     * Calculate total ordered amount for a product using derived query.
     * 
     * <p>Sums up all order quantities for the specified product across
     * all orders. JPA automatically generates optimized aggregation query.</p>
     * 
     * <p>Returns 0 if no orders found (handled by default value).</p>
     * 
     * @param productId ID of the product
     * @return Total quantity ordered (never null, 0 if no orders)
     */
    default Integer getTotalOrderedAmount(Long productId) {
        return sumOrderProductAmountByIdProductId(productId).orElse(0);
    }
    
    /**
     * Internal method to sum order amounts by product ID.
     * JPA automatically generates: SELECT SUM(t.orderProductAmount) 
     * FROM TrProductOrder t WHERE t.id.productId = :productId
     */
    Optional<Integer> sumOrderProductAmountByIdProductId(Long productId);
    
    /**
     * Find maximum order ID using JPQL.
     * 
     * <p>Returns the highest order_id value currently in the system.
     * Used to generate new sequential order IDs. More efficient than
     * fetching full entity as it only selects the ID field.</p>
     * 
     * @return Optional containing maximum order ID, or empty if no orders
     */
    @Query("SELECT MAX(t.id.orderId) FROM TrProductOrder t")
    Optional<Long> findMaxOrderId();
}
