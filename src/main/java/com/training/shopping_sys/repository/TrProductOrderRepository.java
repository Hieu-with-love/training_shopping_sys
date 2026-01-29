package com.training.shopping_sys.repository;

import com.training.shopping_sys.entity.TrProductOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrProductOrderRepository extends JpaRepository<TrProductOrder, Long> {
}
