/**
 * Declares the Hibernate @FilterDef for the restaurantFilter
 * used across all RestaurantAwareEntity subclasses.
 */
@org.hibernate.annotations.FilterDef(
        name = "restaurantFilter",
        parameters = @org.hibernate.annotations.ParamDef(name = "restaurantId", type = Long.class)
)
package com.dts.restro.common.entity;
