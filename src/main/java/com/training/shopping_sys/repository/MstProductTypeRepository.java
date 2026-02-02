package com.training.shopping_sys.repository;

import com.training.shopping_sys.entity.MstProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Product Type Repository.
 * 
 * <p>Data access interface for {@link MstProductType} entity.
 * Provides CRUD operations and custom queries for product type management.</p>
 * 
 * @author Training Team
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface MstProductTypeRepository extends JpaRepository<MstProductType, Long> {
    
    /**
     * Find all active product types.
     * 
     * <p>Retrieves product types with status != '1' (not deleted),
     * ordered alphabetically by product type name.</p>
     * 
     * @return List of active product types sorted by name
     */
    @Query("SELECT pt FROM MstProductType pt WHERE pt.status != '1' ORDER BY pt.producttypeName")
    List<MstProductType> findAllActive();
}
