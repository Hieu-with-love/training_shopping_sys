package com.training.shopping_sys.repository;

import com.training.shopping_sys.entity.MstProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Product Repository.
 * 
 * <p>Data access interface for {@link MstProduct} entity.
 * Extends JpaRepository to provide CRUD operations and custom queries.</p>
 * 
 * <p>Custom queries include:
 * - Product search with keyword filtering and product type filtering
 * - Aggregation of total ordered amounts per product
 * - Sorting by popularity (total orders)
 * </p>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface MstProductRepository extends JpaRepository<MstProduct, Long> {
    
    /**
     * Search products with keyword and product type filter.
     * 
     * <p>Native SQL query that:
     * - Filters active products and product types (status != '1')
     * - Supports case-insensitive keyword search on name and description
     * - Filters by product type if specified
     * - Joins with order table to calculate total ordered amounts
     * - Orders results by popularity (total_ordered DESC)
     * </p>
     * 
     * @param keyword Search keyword for product name/description (optional)
     * @param producttypeId Filter by product type ID (optional)
     * @return List of Object arrays containing product data and total ordered amount
     */
    @Query(value = "SELECT p.product_id, p.product_name, p.product_description, p.product_img, " +
            "p.producttype_id, pt.producttype_name, COALESCE(SUM(o.order_product_amount), 0) as total_ordered " +
            "FROM mstproduct p " +
            "INNER JOIN mstproducttype pt ON p.producttype_id = pt.producttype_id " +
            "LEFT JOIN trproductorder o ON p.product_id = o.product_id " +
            "WHERE p.status != '1' " +
            "AND pt.status != '1' " +
            "AND (:keyword IS NULL OR :keyword = '' OR p.product_name ILIKE %:keyword% OR p.product_description ILIKE %:keyword%) " +
            "AND (:producttypeId IS NULL OR p.producttype_id = :producttypeId) " +
            "GROUP BY p.product_id, p.product_name, p.product_description, p.producttype_id, pt.producttype_name " +
            "ORDER BY total_ordered DESC",
            nativeQuery = true)
    List<Object[]> searchProducts(@Param("keyword") String keyword, 
                                   @Param("producttypeId") Long producttypeId);
}
