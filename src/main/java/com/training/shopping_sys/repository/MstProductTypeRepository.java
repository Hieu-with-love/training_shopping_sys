package com.training.shopping_sys.repository;

import com.training.shopping_sys.entity.MstProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MstProductTypeRepository extends JpaRepository<MstProductType, Long> {
    
    @Query("SELECT pt FROM MstProductType pt WHERE pt.status != '1' ORDER BY pt.producttypeName")
    List<MstProductType> findAllActive();
}
