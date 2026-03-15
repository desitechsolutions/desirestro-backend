package com.dts.restro.common.entity;

import com.dts.restro.common.listener.RestaurantEntityListener;
import com.dts.restro.restaurant.entity.Restaurant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

/**
 * Extends {@link BaseEntity} with a mandatory {@code restaurant} foreign key.
 * <p>
 * All domain entities that belong to a specific restaurant tenant must extend
 * this class. The Hibernate {@code restaurantFilter} is declared on this superclass
 * so it is inherited by every subclass, providing automatic row-level tenant
 * isolation when the filter is enabled via {@link com.dts.restro.common.aspect.RestaurantFilterAspect}.
 * </p>
 * The {@link RestaurantEntityListener} automatically resolves and sets the
 * {@code restaurant} association from {@link com.dts.restro.common.TenantContext}
 * before each persist/update, so callers never have to set it manually.
 */
@MappedSuperclass
@EntityListeners(RestaurantEntityListener.class)
@Filter(name = "restaurantFilter", condition = "restaurant_id = :restaurantId")
@Getter
@Setter
public abstract class RestaurantAwareEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
}
