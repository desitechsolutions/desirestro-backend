package com.dts.restro.repository;

import com.dts.restro.entity.MenuItemIngredient;
import com.dts.restro.entity.MenuItemIngredientId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemIngredientRepository extends JpaRepository<MenuItemIngredient, MenuItemIngredientId> {
    List<MenuItemIngredient> findByMenuItemId(Long menuItemId);
}