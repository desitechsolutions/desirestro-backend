package com.dts.restro.menu.repository;

import com.dts.restro.common.annotation.SkipRestaurantFilter;
import com.dts.restro.menu.entity.MenuItemIngredient;
import com.dts.restro.menu.entity.MenuItemIngredientId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Junction-table repository — isolation is guaranteed via the related MenuItem
 * which carries the restaurantFilter.  Applying the filter here would fail
 * because MenuItemIngredient has no restaurant_id column of its own.
 */
@SkipRestaurantFilter
@Repository
public interface MenuItemIngredientRepository extends JpaRepository<MenuItemIngredient, MenuItemIngredientId> {
    List<MenuItemIngredient> findByMenuItemId(Long menuItemId);
}
