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
 * <p>Key methods:
 * - getTotalOrderedAmount: Tính tổng số lượng đã đặt hàng cho một sản phẩm
 * - findMaxOrderId: Lấy order ID lớn nhất để generate ID mới</p>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface TrProductOrderRepository extends JpaRepository<TrProductOrder, TrProductOrderKey> {
    
    /**
     * Calculate total ordered amount for a product using JPQL.
     * 
     * <p>Sums up all order quantities for the specified product across
     * all orders using JPQL query.</p>
     * 
     * <p>Returns 0 if no orders found (handled by default value).</p>
     * 
     * <p>Được sử dụng để:
     * - Tính số lượng tồn kho khả dụng = product_amount - total_ordered
     * - Validate số lượng đặt hàng không vượt quá tồn kho</p>
     * 
     * @param productId ID of the product
     * @return Total quantity ordered (never null, 0 if no orders)
     */
    default Integer getTotalOrderedAmount(Long productId) {
        return sumOrderProductAmountByProductId(productId).orElse(0);
    }
    
    /**
     * Sum order amounts by product ID using JPQL.
     * 
     * <p>JPQL query:
     * SELECT COALESCE(SUM(t.orderProductAmount), 0) 
     * FROM TrProductOrder t 
     * WHERE t.id.productId = :productId</p>
     * 
     * @param productId ID của sản phẩm
     * @return Optional chứa tổng số lượng đã đặt, hoặc empty nếu chưa có đơn hàng nào
     */
    @Query("SELECT COALESCE(SUM(t.orderProductAmount), 0) FROM TrProductOrder t WHERE t.id.productId = :productId")
    Optional<Integer> sumOrderProductAmountByProductId(@Param("productId") Long productId);
    
    /**
     * Find maximum order ID using JPQL.
     * 
     * <p>Returns the highest order_id value currently in the system.
     * Used to generate new sequential order IDs. More efficient than
     * fetching full entity as it only selects the ID field.</p>
     * 
     * <p>Sử dụng trong OrderService.processOrder() để:
     * - Lấy max order_id hiện tại
     * - Tạo order_id mới = max + 1
     * - Đảm bảo tất cả sản phẩm trong một đơn hàng có cùng order_id</p>
     * 
     * @return Optional containing maximum order ID, or empty if no orders
     */
    @Query("SELECT MAX(t.id.orderId) FROM TrProductOrder t")
    Optional<Long> findMaxOrderId();
}
