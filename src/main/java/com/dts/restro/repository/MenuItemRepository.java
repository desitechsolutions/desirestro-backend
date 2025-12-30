package com.dts.restro.repository;

import com.dts.restro.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByCategoryIdAndAvailableTrue(Long categoryId);
    List<MenuItem> findByAvailableTrue();
}