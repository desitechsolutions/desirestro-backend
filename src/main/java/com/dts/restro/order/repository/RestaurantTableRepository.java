package com.dts.restro.order.repository;
import com.dts.restro.order.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {
    List<RestaurantTable> findAllByOrderByTableNumberAsc();
    Boolean existsByTableNumber(String tableNumber);
    List<RestaurantTable> findByStatus(String status);

    long countByRestaurantId(Long restaurantId);
}