package com.training.shopping_sys.repository;

import com.training.shopping_sys.dto.ProductSearchProjection;
import com.training.shopping_sys.entity.MstProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
 * <p>Key methods:
 * - findById: Lấy thông tin sản phẩm theo ID (từ JpaRepository)
 * - searchProducts: Tìm kiếm sản phẩm với pagination</p>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface MstProductRepository extends JpaRepository<MstProduct, Long> {
    
    /**
     * Search products with keyword and product type filter using JPQL.
     * 
     * <p>JPQL query that:
     * - Filters active products and product types (status != '1')
     * - Supports case-insensitive keyword search on name and description
     * - Filters by product type if specified
     * - Joins with order table to calculate total ordered amounts
     * - Returns paginated results with sorting support
     * </p>
     * 
     * <p>Được sử dụng trong:
     * - ProductService.searchProducts(): Tìm kiếm sản phẩm trên trang product-list
     * - Hiển thị danh sách sản phẩm với thông tin tồn kho</p>
     * 
     * @param keyword Search keyword for product name/description (optional)
     * @param producttypeId Filter by product type ID (optional)
     * @param pageable Pagination and sorting parameters
     * @return Page of ProductSearchProjection containing product data and total ordered amount
     */
    @Query("SELECT p.productId as productId, " +
           "p.productName as productName, " +
           "p.productDescription as productDescription, " +
           "p.productImg as productImg, " +
           "p.producttypeId as producttypeId, " +
           "pt.producttypeName as producttypeName, " +
           "COALESCE(SUM(po.orderProductAmount), 0L) as totalOrdered " +
           "FROM MstProduct p " +
           "INNER JOIN MstProductType pt ON p.producttypeId = pt.producttypeId " +
           "LEFT JOIN TrProductOrder po ON p.productId = po.id.productId " +
           "WHERE p.status <> '1' " +
           "AND pt.status <> '1' " +
           "AND (:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.productDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:producttypeId IS NULL OR p.producttypeId = :producttypeId) " +
           "GROUP BY p.productId, p.productName, p.productDescription, p.productImg, " +
           "p.producttypeId, pt.producttypeName " +
           "ORDER BY totalOrdered DESC")
    Page<ProductSearchProjection> searchProducts(@Param("keyword") String keyword, 
                                                  @Param("producttypeId") Long producttypeId,
                                                  Pageable pageable);
}
