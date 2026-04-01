package com.dts.restro.menu.repository;

import com.dts.restro.menu.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("SELECT COALESCE(MAX(c.displayOrder), 0) FROM Category c")
    Integer findMaxDisplayOrder();

    List<Category> findAllByOrderByDisplayOrderAsc();

    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.items i WHERE i.available = true OR i IS NULL ORDER BY c.displayOrder ASC")
    List<Category> findAllWithAvailableItemsOrdered();
}